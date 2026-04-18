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
@Table(name = "reception_waiting_queue")
@Getter
@Setter
@NoArgsConstructor
public class OutpatientWaitingQueueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RECEPTION_WAITING_QUEUE")
    @SequenceGenerator(
            name = "SEQ_RECEPTION_WAITING_QUEUE",
            sequenceName = "SEQ_RECEPTION_WAITING_QUEUE",
            allocationSize = 1
    )
    @Column(name = "waiting_queue_id")
    private Long waitingQueueId;

    @Column(name = "reception_id", nullable = false)
    private Long receptionId;

    @Column(name = "queue_no", length = 30)
    private String queueNo;

    @Column(name = "queue_status_cd", length = 20, nullable = false)
    private String queueStatusCd = "WAITING";

    @Column(name = "queue_order_no")
    private Long queueOrderNo;

    @Column(name = "dept_id")
    private String deptId;

    @Column(name = "doctor_id")
    private String doctorId;

    @Column(name = "estimated_wait_min")
    private Integer estimatedWaitMin;

    @Column(name = "active_yn", length = 1, nullable = false)
    private String activeYn = "Y";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
