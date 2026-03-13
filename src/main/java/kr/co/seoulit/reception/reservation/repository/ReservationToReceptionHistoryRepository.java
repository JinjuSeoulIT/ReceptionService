package kr.co.seoulit.reception.reservation.repository;

import kr.co.seoulit.reception.reservation.entity.ReservationToReceptionHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationToReceptionHistoryRepository extends JpaRepository<ReservationToReceptionHistoryEntity, Long> {
}
