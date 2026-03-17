package kr.co.seoulit.reception.outpatient.repository;

import kr.co.seoulit.reception.outpatient.entity.ReceptionClosureReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceptionClosureReasonRepository extends JpaRepository<ReceptionClosureReasonEntity, String> {
    List<ReceptionClosureReasonEntity> findByUsableYnOrderBySortOrderAsc(String usableYn);
}
