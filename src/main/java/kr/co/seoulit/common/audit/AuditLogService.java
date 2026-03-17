package kr.co.seoulit.common.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void log(String entityType, Long entityId, String action, Long actorId, String reasonCode, String reasonText, Object before, Object after) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setEntityType(entityType);
        entity.setEntityId(entityId);
        entity.setAction(action);
        entity.setActorId(actorId);
        entity.setReasonCode(reasonCode);
        entity.setReasonText(reasonText);
        entity.setBeforeJson(toJson(before));
        entity.setAfterJson(toJson(after));
        auditLogRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<AuditLogDTO> getByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(entityType, entityId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogDTO> getByActor(Long actorId, LocalDateTime from, LocalDateTime to) {
        return auditLogRepository.findByActorIdAndOccurredAtBetweenOrderByOccurredAtDesc(actorId, from, to)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            log.warn("audit log json serialize failed: {}", ex.getMessage());
            return null;
        }
    }

    private AuditLogDTO toDto(AuditLogEntity entity) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setAuditLogId(entity.getAuditLogId());
        dto.setEntityType(entity.getEntityType());
        dto.setEntityId(entity.getEntityId());
        dto.setAction(entity.getAction());
        dto.setActorId(entity.getActorId());
        dto.setOccurredAt(entity.getOccurredAt());
        dto.setReasonCode(entity.getReasonCode());
        dto.setReasonText(entity.getReasonText());
        dto.setBeforeJson(entity.getBeforeJson());
        dto.setAfterJson(entity.getAfterJson());
        dto.setDiffJson(entity.getDiffJson());
        return dto;
    }
}



