package kr.co.seoulit.reception.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
public class ReservationReceptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RESERVATION")
    @SequenceGenerator(name = "SEQ_RESERVATION", sequenceName = "SEQ_RESERVATION", allocationSize = 1)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "reservation_no", length = 30, nullable = false, unique = true)
    private String reservationNo;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Transient
    private String patientName;

    @Column(name = "department_id", nullable = false)
    private String departmentId;

    @Transient
    private String departmentName;

    @Column(name = "doctor_id")
    private String doctorId;

    @Transient
    private String doctorName;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "RESERVED";

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

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "cancel_reason_code", length = 30)
    private String cancelReasonCode;

    @Column(name = "cancel_reason_text", length = 255)
    private String cancelReasonText;

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


