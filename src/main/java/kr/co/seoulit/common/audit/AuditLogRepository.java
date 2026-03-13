package kr.co.seoulit.common.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    List<AuditLogEntity> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(String entityType, Long entityId);

    List<AuditLogEntity> findByActorIdAndOccurredAtBetweenOrderByOccurredAtDesc(
            Long actorId,
            LocalDateTime from,
            LocalDateTime to
    );
}

