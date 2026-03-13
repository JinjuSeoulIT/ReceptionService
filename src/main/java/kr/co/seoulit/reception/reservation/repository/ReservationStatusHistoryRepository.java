package kr.co.seoulit.reception.reservation.repository;

import kr.co.seoulit.reception.reservation.entity.ReservationStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationStatusHistoryRepository extends JpaRepository<ReservationStatusHistoryEntity, Long> {
}
