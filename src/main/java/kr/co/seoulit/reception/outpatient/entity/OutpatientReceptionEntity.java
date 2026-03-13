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
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reception")
@Getter
@Setter
@NoArgsConstructor
public class ReceptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RECEPTION")
    @SequenceGenerator(name = "SEQ_RECEPTION", sequenceName = "SEQ_RECEPTION", allocationSize = 1)
    @Column(name = "reception_id")
    private Long receptionId;

    @Column(name = "reception_no", length = 30, nullable = false, unique = true)
    private String receptionNo;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "patient_name", length = 50, nullable = false)
    private String patientName;

    @Column(name = "visit_type", length = 20, nullable = false)
    private String visitType = "OUTPATIENT";

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "department_name", length = 50, nullable = false)
    private String departmentName;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "doctor_name", length = 50)
    private String doctorName;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "WAITING";

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "inactive_at")
    private LocalDateTime inactiveAt;

    @Column(name = "inactive_reason_code", length = 30)
    private String inactiveReasonCode;

    @Column(name = "inactive_reason_text", length = 255)
    private String inactiveReasonText;

    @Column(name = "cancel_reason_code", length = 30)
    private String cancelReasonCode;

    @Column(name = "cancel_reason_text", length = 255)
    private String cancelReasonText;

    @Column(name = "hold_reason_code", length = 30)
    private String holdReasonCode;

    @Column(name = "hold_reason_text", length = 255)
    private String holdReasonText;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/entity/ReceptionEntity.java
========



>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/outpatient/entity/OutpatientReceptionEntity.java
