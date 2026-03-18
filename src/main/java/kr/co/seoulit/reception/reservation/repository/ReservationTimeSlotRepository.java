package kr.co.seoulit.reception.reservation.repository;

import kr.co.seoulit.reception.reservation.entity.ReservationTimeSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationTimeSlotRepository extends JpaRepository<ReservationTimeSlotEntity, Long> {
    Optional<ReservationTimeSlotEntity> findTopByReservationIdOrderByTimeSlotIdDesc(Long reservationId);
}
