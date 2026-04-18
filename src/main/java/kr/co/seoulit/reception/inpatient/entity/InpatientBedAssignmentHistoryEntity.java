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
@Table(name = "inpatient_bed_assignment_his")
@Getter
@Setter
@NoArgsConstructor
public class InpatientBedAssignmentHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INPT_BED_ASSIGN_HIS")
    @SequenceGenerator(name = "SEQ_INPT_BED_ASSIGN_HIS", sequenceName = "SEQ_INPT_BED_ASSIGN_HIS", allocationSize = 1)
    @Column(name = "bed_assignment_history_id")
    private Long bedAssignmentHistoryId;

    @Column(name = "bed_assignment_id", nullable = false)
    private Long bedAssignmentId;

    @Column(name = "before_bed_id")
    private Long beforeBedId;

    @Column(name = "after_bed_id")
    private Long afterBedId;

    @Column(name = "change_reason", length = 1000)
    private String changeReason;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}
