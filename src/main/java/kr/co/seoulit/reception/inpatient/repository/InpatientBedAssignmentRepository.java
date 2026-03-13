package kr.co.seoulit.reception.inpatient.repository;

import kr.co.seoulit.reception.inpatient.entity.InpatientBedAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InpatientBedAssignmentRepository extends JpaRepository<InpatientBedAssignmentEntity, Long> {
    Optional<InpatientBedAssignmentEntity> findTopByInpatientAdmissionIdOrderByAssignmentDatetimeDesc(Long inpatientAdmissionId);
}


