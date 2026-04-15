package kr.co.seoulit.reception.reservation.repository;

import kr.co.seoulit.reception.reservation.entity.ReservationReceptionEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationReceptionRepository extends JpaRepository<ReservationReceptionEntity, Long> {
    boolean existsByReservationNo(String reservationNo);

    Optional<ReservationReceptionEntity> findByReservationNo(String reservationNo);

    List<ReservationReceptionEntity> findAllByStatusAndIsActiveTrueAndReservedAtBetweenOrderByReservationIdAsc(
            String status,
            LocalDateTime reservedAtFrom,
            LocalDateTime reservedAtTo
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ReservationReceptionEntity> findByReservationId(Long reservationId);
}




