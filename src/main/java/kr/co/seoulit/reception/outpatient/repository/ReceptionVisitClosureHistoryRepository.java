package kr.co.seoulit.reception.outpatient.repository;

import kr.co.seoulit.reception.outpatient.entity.ReceptionVisitClosureHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceptionVisitClosureHistoryRepository extends JpaRepository<ReceptionVisitClosureHistoryEntity, Long> {
    List<ReceptionVisitClosureHistoryEntity> findByVisitClosureIdOrderByChangedAtDesc(Long visitClosureId);
}
