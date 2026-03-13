package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.entity.ReceptionStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceptionStatusHistoryRepository extends JpaRepository<ReceptionStatusHistoryEntity, Long> {
    List<ReceptionStatusHistoryEntity> findByReceptionIdOrderByChangedAtAsc(Long receptionId);
}
