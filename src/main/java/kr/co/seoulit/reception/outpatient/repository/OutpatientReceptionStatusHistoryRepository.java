package kr.co.seoulit.reception.outpatient.repository;

import kr.co.seoulit.reception.outpatient.entity.OutpatientReceptionStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutpatientReceptionStatusHistoryRepository extends JpaRepository<OutpatientReceptionStatusHistoryEntity, Long> {
    List<OutpatientReceptionStatusHistoryEntity> findByReceptionIdOrderByChangedAtAsc(Long receptionId);
}




