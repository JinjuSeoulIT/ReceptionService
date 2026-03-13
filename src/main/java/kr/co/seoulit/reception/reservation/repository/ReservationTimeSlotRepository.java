package kr.co.seoulit.reception.reservation.repository;

import kr.co.seoulit.reception.reservation.entity.ReservationTimeSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTimeSlotRepository extends JpaRepository<ReservationTimeSlotEntity, Long> {
}
