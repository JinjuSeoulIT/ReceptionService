package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {
}
