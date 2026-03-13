package kr.co.seoulit.reception.inpatient.repository;

import kr.co.seoulit.reception.inpatient.entity.InpatientAdmissionDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InpatientAdmissionDecisionRepository extends JpaRepository<InpatientAdmissionDecisionEntity, Long> {
    Optional<InpatientAdmissionDecisionEntity> findTopByInpatientAdmissionIdOrderByDecisionDatetimeDesc(Long inpatientAdmissionId);
}
