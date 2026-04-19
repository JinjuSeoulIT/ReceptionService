package kr.co.seoulit.reception.service;

import kr.co.seoulit.reception.dto.DepartmentDTO;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    List<DepartmentDTO> getActiveDepartments();

    Optional<String> findDepartmentName(String departmentId);
}
