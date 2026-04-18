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
@Table(name = "inpatient_bed_assignment")
@Getter
@Setter
@NoArgsConstructor
public class InpatientBedAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INPT_BED_ASSIGNMENT")
    @SequenceGenerator(name = "SEQ_INPT_BED_ASSIGNMENT", sequenceName = "SEQ_INPT_BED_ASSIGNMENT", allocationSize = 1)
    @Column(name = "bed_assignment_id")
    private Long bedAssignmentId;

    @Column(name = "inpatient_admission_id", nullable = false)
    private Long inpatientAdmissionId;

    @Column(name = "ward_id")
    private Long wardId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "bed_id")
    private Long bedId;

    @Column(name = "assignment_datetime", nullable = false)
    private LocalDateTime assignmentDatetime;

    @Column(name = "assignment_status_cd", length = 30, nullable = false)
    private String assignmentStatusCd;

    @Column(name = "assigned_by")
    private String assignedBy;

    @Column(name = "remark", length = 1000)
    private String remark;
}

