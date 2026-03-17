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

import java.time.LocalDateTime;

@Entity
@Table(name = "reception_visit_closure_his")
@Getter
@Setter
@NoArgsConstructor
public class ReceptionVisitClosureHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RECEPTION_VISIT_CLS_HIS")
    @SequenceGenerator(
            name = "SEQ_RECEPTION_VISIT_CLS_HIS",
            sequenceName = "SEQ_RECEPTION_VISIT_CLS_HIS",
            allocationSize = 1
    )
    @Column(name = "visit_closure_history_id")
    private Long visitClosureHistoryId;

    @Column(name = "visit_closure_id", nullable = false)
    private Long visitClosureId;

    @Column(name = "before_status_cd", length = 20)
    private String beforeStatusCd;

    @Column(name = "after_status_cd", length = 20)
    private String afterStatusCd;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "change_reason", length = 1000)
    private String changeReason;
}
