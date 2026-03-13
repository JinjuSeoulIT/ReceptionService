package kr.co.seoulit.reception.inpatient.repository;

import kr.co.seoulit.reception.inpatient.entity.InpatientAdmissionAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InpatientAdmissionAuditRepository extends JpaRepository<InpatientAdmissionAuditEntity, Long> {
}
