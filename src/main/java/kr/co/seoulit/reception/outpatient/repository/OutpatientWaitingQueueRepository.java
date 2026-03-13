package kr.co.seoulit.reception.outpatient.repository;

import kr.co.seoulit.reception.outpatient.entity.OutpatientWaitingQueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OutpatientWaitingQueueRepository extends JpaRepository<OutpatientWaitingQueueEntity, Long> {
    Optional<OutpatientWaitingQueueEntity> findByReceptionId(Long receptionId);
}
