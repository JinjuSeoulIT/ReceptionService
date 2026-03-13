package kr.co.seoulit.reception.inpatient.repository;

import kr.co.seoulit.reception.inpatient.entity.InpatientBedAssignmentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InpatientBedAssignmentHistoryRepository extends JpaRepository<InpatientBedAssignmentHistoryEntity, Long> {
}
