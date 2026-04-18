package kr.co.seoulit.reception.inpatient.entity;

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
@Table(name = "inpatient_admission_audit")
@Getter
@Setter
@NoArgsConstructor
public class InpatientAdmissionAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INPT_ADM_AUDIT")
    @SequenceGenerator(name = "SEQ_INPT_ADM_AUDIT", sequenceName = "SEQ_INPT_ADM_AUDIT", allocationSize = 1)
    @Column(name = "inpatient_admission_audit_id")
    private Long inpatientAdmissionAuditId;

    @Column(name = "inpatient_admission_id", nullable = false)
    private Long inpatientAdmissionId;

    @Column(name = "change_field_nm", length = 100)
    private String changeFieldNm;

    @Column(name = "before_value", length = 4000)
    private String beforeValue;

    @Column(name = "after_value", length = 4000)
    private String afterValue;

    @Column(name = "change_reason", length = 1000)
    private String changeReason;

    @Column(name = "changed_by")
    private String changedBy;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false, nullable = false)
    private LocalDateTime changedAt;
}
