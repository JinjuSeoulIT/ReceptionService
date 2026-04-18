package kr.co.seoulit.reception.reservation.repository;

import kr.co.seoulit.reception.reservation.entity.ReservationBookingRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationBookingRuleRepository extends JpaRepository<ReservationBookingRuleEntity, Long> {
    Optional<ReservationBookingRuleEntity> findTopByDeptIdAndDoctorIdOrderByBookingRuleIdDesc(String deptId, String doctorId);
}
