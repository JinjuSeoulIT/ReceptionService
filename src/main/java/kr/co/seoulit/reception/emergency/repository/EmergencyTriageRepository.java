package kr.co.seoulit.reception.emergency.repository;

import kr.co.seoulit.reception.emergency.entity.EmergencyTriageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmergencyTriageRepository extends JpaRepository<EmergencyTriageEntity, Long> {
    Optional<EmergencyTriageEntity> findTopByReceptionIdOrderByTriageDatetimeDesc(Long receptionId);
}


