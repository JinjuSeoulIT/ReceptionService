package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.outpatient.entity.OutpatientReceptionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceptionRepository extends CrudRepository<OutpatientReceptionEntity, Long> {

    boolean existsByReceptionNo(String receptionNo);

    List<OutpatientReceptionEntity> findByPatientId(Long patientId);
}

