package kr.co.seoulit.reception.outpatient.repository;

import kr.co.seoulit.reception.outpatient.entity.ReceptionQualificationItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceptionQualificationItemRepository extends JpaRepository<ReceptionQualificationItemEntity, Long> {
    List<ReceptionQualificationItemEntity> findByQualificationSnapshotIdOrderByDisplayOrderAsc(Long qualificationSnapshotId);

    void deleteByQualificationSnapshotId(Long qualificationSnapshotId);
}
