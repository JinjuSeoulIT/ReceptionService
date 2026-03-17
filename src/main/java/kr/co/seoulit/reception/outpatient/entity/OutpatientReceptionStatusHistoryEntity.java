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

import java.time.LocalDateTime;

@Entity
@Table(name = "reception_status_history")
@Getter
@Setter
@NoArgsConstructor
public class OutpatientReceptionStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RECEPTION_STATUS_HISTORY")
    @SequenceGenerator(name = "SEQ_RECEPTION_STATUS_HISTORY", sequenceName = "SEQ_RECEPTION_STATUS_HISTORY", allocationSize = 1)
    @Column(name = "status_history_id")
    private Long statusHistoryId;

    @Column(name = "reception_id", nullable = false)
    private Long receptionId;

    @Column(name = "from_status", length = 20)
    private String fromStatus;

    @Column(name = "to_status", length = 20, nullable = false)
    private String toStatus;

    @Column(name = "changed_by")
    private Long changedBy;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;

    @Column(name = "reason_code", length = 30)
    private String reasonCode;

    @Column(name = "reason_text", length = 255)
    private String reasonText;
}



