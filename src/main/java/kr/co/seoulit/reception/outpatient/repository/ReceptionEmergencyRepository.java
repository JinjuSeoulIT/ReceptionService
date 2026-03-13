package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.entity.ReceptionEmergencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceptionEmergencyRepository extends JpaRepository<ReceptionEmergencyEntity, Long> {
}
