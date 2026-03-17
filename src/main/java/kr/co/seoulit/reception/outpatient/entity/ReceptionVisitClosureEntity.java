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
@Table(name = "reception_visit_closure")
@Getter
@Setter
@NoArgsConstructor
public class ReceptionVisitClosureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RECEPTION_VISIT_CLOSURE")
    @SequenceGenerator(
            name = "SEQ_RECEPTION_VISIT_CLOSURE",
            sequenceName = "SEQ_RECEPTION_VISIT_CLOSURE",
            allocationSize = 1
    )
    @Column(name = "visit_closure_id")
    private Long visitClosureId;

    @Column(name = "reception_id", nullable = false)
    private Long receptionId;

    @Column(name = "closure_status_cd", length = 20, nullable = false)
    private String closureStatusCd;

    @Column(name = "closure_datetime", nullable = false)
    private LocalDateTime closureDatetime;

    @Column(name = "closure_user_id")
    private Long closureUserId;

    @Column(name = "closure_reason_cd", length = 30)
    private String closureReasonCd;

    @Column(name = "remark", length = 1000)
    private String remark;

    @Column(name = "active_yn", length = 1, nullable = false)
    private String activeYn = "Y";
}
