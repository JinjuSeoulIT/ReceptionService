package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.entity.ReceptionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceptionRepository extends CrudRepository<ReceptionEntity, Long> {

    boolean existsByReceptionNo(String receptionNo);

    List<ReceptionEntity> findByPatientId(Long patientId);
}

