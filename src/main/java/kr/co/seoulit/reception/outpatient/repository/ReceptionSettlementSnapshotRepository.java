package kr.co.seoulit.reception.outpatient.repository;

import kr.co.seoulit.reception.outpatient.entity.ReceptionSettlementSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReceptionSettlementSnapshotRepository extends JpaRepository<ReceptionSettlementSnapshotEntity, Long> {
    Optional<ReceptionSettlementSnapshotEntity> findTopByReceptionIdOrderBySnapshotDatetimeDesc(Long receptionId);

    List<ReceptionSettlementSnapshotEntity> findByReceptionIdOrderBySnapshotDatetimeDesc(Long receptionId);
}
