package kr.co.seoulit.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AUDIT_LOG")
    @SequenceGenerator(name = "SEQ_AUDIT_LOG", sequenceName = "SEQ_AUDIT_LOG", allocationSize = 1)
    @Column(name = "audit_log_id")
    private Long auditLogId;

    @Column(name = "entity_type", length = 20, nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", length = 20, nullable = false)
    private String action;

    @Column(name = "actor_id")
    private Long actorId;

    @CreationTimestamp
    @Column(name = "occurred_at", updatable = false)
    private LocalDateTime occurredAt;

    @Column(name = "reason_code", length = 30)
    private String reasonCode;

    @Column(name = "reason_text", length = 255)
    private String reasonText;

    @Column(name = "before_json", columnDefinition = "json")
    private String beforeJson;

    @Column(name = "after_json", columnDefinition = "json")
    private String afterJson;

    @Column(name = "diff_json", columnDefinition = "json")
    private String diffJson;
}

