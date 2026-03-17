package kr.co.seoulit.reception.outpatient.repository;

import kr.co.seoulit.reception.outpatient.entity.ReceptionCallHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReceptionCallHistoryRepository extends JpaRepository<ReceptionCallHistoryEntity, Long> {
    Optional<ReceptionCallHistoryEntity> findTopByWaitingQueueIdOrderByCallDatetimeDesc(Long waitingQueueId);

    List<ReceptionCallHistoryEntity> findByWaitingQueueIdOrderByCallDatetimeDesc(Long waitingQueueId);
}
