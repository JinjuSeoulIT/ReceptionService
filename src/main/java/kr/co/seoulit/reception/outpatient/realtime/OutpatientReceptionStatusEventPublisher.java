package kr.co.seoulit.reception.outpatient.realtime;
// 실제 Eventstream 수행 주체
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusChangedEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class OutpatientReceptionStatusEventPublisher {

    private static final long SSE_TIMEOUT_MS = 0L;
    private static final String EVENT_CONNECTED = "connected";
    private static final String EVENT_STATUS_CHANGED = "reception-status-changed";

    private final ConcurrentMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe() {
        String emitterId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitters.put(emitterId, emitter);

        emitter.onCompletion(() -> removeEmitter(emitterId));
        emitter.onTimeout(() -> removeEmitter(emitterId));
        emitter.onError((ex) -> removeEmitter(emitterId));

        sendConnectedEvent(emitterId, emitter);
        log.info("SSE subscribed: emitterId={}, activeClients={}", emitterId, emitters.size());
        return emitter;
    }

    public void publishStatusChanged(OutpatientReceptionStatusChangedEventDTO event) {
        emitters.forEach((emitterId, emitter) -> sendEvent(emitterId, emitter, event));
    }

    private void sendConnectedEvent(String emitterId, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name(EVENT_CONNECTED)
                    .id(emitterId)
                    .data(Map.of(
                            "connected", true,
                            "timestamp", LocalDateTime.now().toString()
                    )));
        } catch (IOException ex) {
            removeEmitter(emitterId);
            log.debug("SSE connected event send failed: emitterId={}", emitterId);
        }
    }

    private void sendEvent(
            String emitterId,
            SseEmitter emitter,
            OutpatientReceptionStatusChangedEventDTO event
    ) {
        try {
            emitter.send(SseEmitter.event()
                    .name(EVENT_STATUS_CHANGED)
                    .data(event));
        } catch (Exception ex) {
            removeEmitter(emitterId);
            log.debug("SSE status event send failed: emitterId={}, receptionId={}", emitterId, event.getReceptionId());
        }
    }

    private void removeEmitter(String emitterId) {
        emitters.remove(emitterId);
    }
}
