package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    boolean existsByReservationNo(String reservationNo);
}
