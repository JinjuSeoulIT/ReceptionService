package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.entity.ReceptionTodayScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReceptionTodayScheduleRepository extends JpaRepository<ReceptionTodayScheduleEntity, Long> {
    List<ReceptionTodayScheduleEntity> findByScheduleDateOrderBySortOrderAsc(LocalDate scheduleDate);
}
