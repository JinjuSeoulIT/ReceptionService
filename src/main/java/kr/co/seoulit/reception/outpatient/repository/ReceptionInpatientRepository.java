package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.entity.ReceptionInpatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceptionInpatientRepository extends JpaRepository<ReceptionInpatientEntity, Long> {
}
