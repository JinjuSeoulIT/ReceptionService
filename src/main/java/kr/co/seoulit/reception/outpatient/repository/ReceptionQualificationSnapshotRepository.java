package kr.co.seoulit.reception.outpatient.repository;

import kr.co.seoulit.reception.outpatient.entity.ReceptionQualificationSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReceptionQualificationSnapshotRepository extends JpaRepository<ReceptionQualificationSnapshotEntity, Long> {
    Optional<ReceptionQualificationSnapshotEntity> findTopByReceptionIdOrderBySnapshotDatetimeDesc(Long receptionId);

    List<ReceptionQualificationSnapshotEntity> findByReceptionIdOrderBySnapshotDatetimeDesc(Long receptionId);
}
