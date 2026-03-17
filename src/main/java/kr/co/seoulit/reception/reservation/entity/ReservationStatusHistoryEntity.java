package kr.co.seoulit.reception.reservation.entity;

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
@Table(name = "reservation_status_history")
@Getter
@Setter
@NoArgsConstructor
public class ReservationStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RESERVATION_STATUS_HIS")
    @SequenceGenerator(
            name = "SEQ_RESERVATION_STATUS_HIS",
            sequenceName = "SEQ_RESERVATION_STATUS_HIS",
            allocationSize = 1
    )
    @Column(name = "reservation_status_history_id")
    private Long reservationStatusHistoryId;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "before_status_cd", length = 20)
    private String beforeStatusCd;

    @Column(name = "after_status_cd", length = 20, nullable = false)
    private String afterStatusCd;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "change_reason", length = 1000)
    private String changeReason;
}
