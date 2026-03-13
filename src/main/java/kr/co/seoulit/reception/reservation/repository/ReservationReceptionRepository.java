package kr.co.seoulit.reception.reservation.repository;

import kr.co.seoulit.reception.reservation.entity.ReservationReceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationReceptionRepository extends JpaRepository<ReservationReceptionEntity, Long> {
    boolean existsByReservationNo(String reservationNo);
}




