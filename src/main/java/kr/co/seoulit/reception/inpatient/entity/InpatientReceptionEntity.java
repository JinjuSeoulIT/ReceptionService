package kr.co.seoulit.reception.inpatient.entity;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "inpatient_admission")
@Getter
@Setter
@NoArgsConstructor
public class InpatientReceptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INPATIENT_ADMISSION")
    @SequenceGenerator(name = "SEQ_INPATIENT_ADMISSION", sequenceName = "SEQ_INPATIENT_ADMISSION", allocationSize = 1)
    @Column(name = "inpatient_admission_id")
    private Long inpatientAdmissionId;

    @Column(name = "reception_id")
    private Long receptionId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "admission_status_cd", length = 30, nullable = false)
    private String admissionStatusCd = "WAITING";

    @Column(name = "admission_type_cd", length = 30)
    private String admissionTypeCd;

    @Column(name = "admission_datetime", nullable = false)
    private LocalDateTime admissionPlanAt;

    @Column(name = "admission_reason", length = 1000)
    private String admissionReason;

    @Column(name = "dept_id")
    private String departmentId;

    @Column(name = "attending_doctor_id")
    private String doctorId;

    @Column(name = "active_yn", length = 1, nullable = false)
    private String activeYn = "Y";

    @Transient
    private Long wardId;

    @Transient
    private Long roomId;
}



