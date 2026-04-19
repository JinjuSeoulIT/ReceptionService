package kr.co.seoulit.reception.service;

import kr.co.seoulit.reception.dto.DepartmentDTO;
import kr.co.seoulit.reception.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    @Override
    public List<DepartmentDTO> getActiveDepartments() {
        return departmentMapper.selectActiveDepartments();
    }

    @Override
    public Optional<String> findDepartmentName(String departmentId) {
        return Optional.ofNullable(
                departmentMapper.selectDepartmentNameById(trimToNull(departmentId))
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
