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
@Table(name = "reservation_time_slot")
@Getter
@Setter
@NoArgsConstructor
public class ReservationTimeSlotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RESERVATION_TIME_SLOT")
    @SequenceGenerator(
            name = "SEQ_RESERVATION_TIME_SLOT",
            sequenceName = "SEQ_RESERVATION_TIME_SLOT",
            allocationSize = 1
    )
    @Column(name = "time_slot_id")
    private Long timeSlotId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "slot_start_datetime", nullable = false)
    private LocalDateTime slotStartDatetime;

    @Column(name = "slot_end_datetime", nullable = false)
    private LocalDateTime slotEndDatetime;

    @Column(name = "slot_status_cd", length = 20, nullable = false)
    private String slotStatusCd;

    @Column(name = "reservation_id")
    private Long reservationId;
}
