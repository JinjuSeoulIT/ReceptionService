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
@Table(name = "reception_call_history")
@Getter
@Setter
@NoArgsConstructor
public class ReceptionCallHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RECEPTION_CALL_HIS")
    @SequenceGenerator(name = "SEQ_RECEPTION_CALL_HIS", sequenceName = "SEQ_RECEPTION_CALL_HIS", allocationSize = 1)
    @Column(name = "call_history_id")
    private Long callHistoryId;

    @Column(name = "waiting_queue_id", nullable = false)
    private Long waitingQueueId;

    @Column(name = "call_datetime", nullable = false)
    private LocalDateTime callDatetime;

    @Column(name = "call_user_id")
    private Long callUserId;

    @Column(name = "call_count", nullable = false)
    private Integer callCount = 1;

    @Column(name = "call_result_cd", length = 30)
    private String callResultCd;

    @Column(name = "remark", length = 1000)
    private String remark;
}
