package kr.co.seoulit.reception.outpatient.repository;

import kr.co.seoulit.reception.outpatient.entity.ReceptionAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceptionAuditRepository extends JpaRepository<ReceptionAuditEntity, Long> {
    List<ReceptionAuditEntity> findByReceptionIdOrderByChangedAtDesc(Long receptionId);
}
