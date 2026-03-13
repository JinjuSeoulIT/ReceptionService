package kr.co.seoulit.reception.inpatient.repository;

import kr.co.seoulit.reception.inpatient.entity.InpatientReceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InpatientReceptionRepository extends JpaRepository<InpatientReceptionEntity, Long> {
    Optional<InpatientReceptionEntity> findByReceptionId(Long receptionId);
}




