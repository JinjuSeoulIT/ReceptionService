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

import java.time.LocalDateTime;

@Entity
@Table(name = "inpatient_admission_decision")
@Getter
@Setter
@NoArgsConstructor
public class InpatientAdmissionDecisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INPT_ADM_DECISION")
    @SequenceGenerator(name = "SEQ_INPT_ADM_DECISION", sequenceName = "SEQ_INPT_ADM_DECISION", allocationSize = 1)
    @Column(name = "decision_id")
    private Long decisionId;

    @Column(name = "inpatient_admission_id", nullable = false)
    private Long inpatientAdmissionId;

    @Column(name = "decision_datetime", nullable = false)
    private LocalDateTime decisionDatetime;

    @Column(name = "decision_doctor_id")
    private String decisionDoctorId;

    @Column(name = "decision_reason", length = 1000)
    private String decisionReason;

    @Column(name = "expected_days")
    private Integer expectedDays;

    @Column(name = "decision_note", length = 1000)
    private String decisionNote;
}
