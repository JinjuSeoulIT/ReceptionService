package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.reservation.entity.ReservationReceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<ReservationReceptionEntity, Long> {
    boolean existsByReservationNo(String reservationNo);
}
