package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.entity.ReceptionChecklistItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReceptionChecklistItemRepository extends JpaRepository<ReceptionChecklistItemEntity, Long> {
    List<ReceptionChecklistItemEntity> findByCheckDateOrderBySortOrderAsc(LocalDate checkDate);
}
