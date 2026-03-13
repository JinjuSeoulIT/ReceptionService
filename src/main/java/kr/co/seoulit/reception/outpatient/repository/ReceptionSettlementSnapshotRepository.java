package kr.co.seoulit.reception.outpatient.repository;

import kr.co.seoulit.reception.outpatient.entity.ReceptionSettlementSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceptionSettlementSnapshotRepository extends JpaRepository<ReceptionSettlementSnapshotEntity, Long> {
}
