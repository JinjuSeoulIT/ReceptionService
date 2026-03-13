package kr.co.seoulit.reception.outpatient.repository;

import kr.co.seoulit.reception.outpatient.entity.ReceptionVisitClosureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceptionVisitClosureRepository extends JpaRepository<ReceptionVisitClosureEntity, Long> {
    Optional<ReceptionVisitClosureEntity> findByReceptionId(Long receptionId);
}
