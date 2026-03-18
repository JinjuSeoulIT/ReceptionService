package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.entity.DoctorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<DoctorEntity, Long> {
    List<DoctorEntity> findByDepartmentId(Long departmentId);
}
