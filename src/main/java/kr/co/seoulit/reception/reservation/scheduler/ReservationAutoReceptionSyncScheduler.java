package kr.co.seoulit.reception.reservation.scheduler;

import kr.co.seoulit.reception.reservation.application.ReservationAutoReceptionSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationAutoReceptionSyncScheduler {

    private final ReservationAutoReceptionSyncService reservationAutoReceptionSyncService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        runSync("startup");
    }

    @Scheduled(cron = "${reception.reservation-auto-sync.cron:0 * * * * *}", zone = "Asia/Seoul")
    public void syncOnSchedule() {
        runSync("schedule");
    }

    private void runSync(String trigger) {
        if (!running.compareAndSet(false, true)) {
            log.info("Skipping reservation auto-sync on {} because a previous run is still in progress", trigger);
            return;
        }

        try {
            log.info("Starting reservation auto-sync on {}", trigger);
            reservationAutoReceptionSyncService.syncTodayReservations();
        } catch (Exception ex) {
            log.error("Reservation auto-sync failed on {}", trigger, ex);
        } finally {
            running.set(false);
        }
    }
}
