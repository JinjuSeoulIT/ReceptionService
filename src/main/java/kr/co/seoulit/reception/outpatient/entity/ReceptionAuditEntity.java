package kr.co.seoulit.reception.outpatient.entity;

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
@Table(name = "reception_audit")
@Getter
@Setter
@NoArgsConstructor
public class ReceptionAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RECEPTION_AUDIT")
    @SequenceGenerator(name = "SEQ_RECEPTION_AUDIT", sequenceName = "SEQ_RECEPTION_AUDIT", allocationSize = 1)
    @Column(name = "reception_audit_id")
    private Long receptionAuditId;

    @Column(name = "reception_id", nullable = false)
    private Long receptionId;

    @Column(name = "change_type_cd", length = 30, nullable = false)
    private String changeTypeCd;

    @Column(name = "change_field_nm", length = 100)
    private String changeFieldNm;

    @Column(name = "before_value", length = 4000)
    private String beforeValue;

    @Column(name = "after_value", length = 4000)
    private String afterValue;

    @Column(name = "change_reason", length = 1000)
    private String changeReason;

    @Column(name = "changed_by")
    private Long changedBy;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false, nullable = false)
    private LocalDateTime changedAt;
}
