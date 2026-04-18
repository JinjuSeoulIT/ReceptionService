package kr.co.seoulit.reception.reservation.repository;

import kr.co.seoulit.reception.reservation.entity.ReservationDoctorScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ReservationDoctorScheduleRepository extends JpaRepository<ReservationDoctorScheduleEntity, Long> {
    Optional<ReservationDoctorScheduleEntity> findTopByDoctorIdAndDeptIdAndScheduleDateOrderByScheduleIdDesc(
            String doctorId,
            String deptId,
            LocalDate scheduleDate
    );
}
