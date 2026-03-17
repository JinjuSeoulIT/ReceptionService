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
@Table(name = "reservation_to_reception_his")
@Getter
@Setter
@NoArgsConstructor
public class ReservationToReceptionHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RESV_TO_RECEPTION_HIS")
    @SequenceGenerator(
            name = "SEQ_RESV_TO_RECEPTION_HIS",
            sequenceName = "SEQ_RESV_TO_RECEPTION_HIS",
            allocationSize = 1
    )
    @Column(name = "reservation_reception_his_id")
    private Long reservationReceptionHisId;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "reception_id", nullable = false)
    private Long receptionId;

    @Column(name = "converted_at", nullable = false)
    private LocalDateTime convertedAt;

    @Column(name = "converted_by")
    private Long convertedBy;

    @Column(name = "result_cd", length = 30)
    private String resultCd;

    @Column(name = "message", length = 1000)
    private String message;
}
