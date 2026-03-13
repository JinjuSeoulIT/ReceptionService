package kr.co.seoulit.reception.emergency.repository;

import kr.co.seoulit.reception.emergency.entity.EmergencyReceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmergencyReceptionRepository extends JpaRepository<EmergencyReceptionEntity, Long> {
    Optional<EmergencyReceptionEntity> findByReceptionId(Long receptionId);
}




