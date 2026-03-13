package kr.co.seoulit.reception.outpatient.repository;

import kr.co.seoulit.reception.outpatient.entity.ReceptionClosureReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceptionClosureReasonRepository extends JpaRepository<ReceptionClosureReasonEntity, String> {
}
