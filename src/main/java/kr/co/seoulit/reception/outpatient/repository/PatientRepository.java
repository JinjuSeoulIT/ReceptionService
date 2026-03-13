package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
    Optional<PatientEntity> findByPatientName(String patientName);
}
