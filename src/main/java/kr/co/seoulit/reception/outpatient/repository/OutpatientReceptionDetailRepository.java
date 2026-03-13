package kr.co.seoulit.reception.outpatient.repository;

import kr.co.seoulit.reception.outpatient.entity.OutpatientReceptionDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OutpatientReceptionDetailRepository extends JpaRepository<OutpatientReceptionDetailEntity, Long> {
    Optional<OutpatientReceptionDetailEntity> findByReceptionId(Long receptionId);
}
