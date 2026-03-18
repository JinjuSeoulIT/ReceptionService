package kr.co.seoulit.reception.reservation.repository;

import kr.co.seoulit.reception.reservation.entity.ReservationReceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationReceptionRepository extends JpaRepository<ReservationReceptionEntity, Long> {
    boolean existsByReservationNo(String reservationNo);

    Optional<ReservationReceptionEntity> findByReservationNo(String reservationNo);
}




