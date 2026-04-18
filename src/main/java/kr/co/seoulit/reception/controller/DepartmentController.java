package kr.co.seoulit.reception.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.dto.DepartmentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/departments")
@Tag(name = "진료과 API", description = "진료과 API")
public class DepartmentController {

    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "진료과 목록 조회", description = "CMH.STAFF_DEPARTMENT 기준 진료과 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getDepartments() {
        String sql = """
                SELECT
                    d.department_id,
                    d.department_name
                FROM CMH.STAFF_DEPARTMENT d
                WHERE EXISTS (
                    SELECT 1
                    FROM CMH.STAFF s
                    WHERE s.staff_department_id = d.department_id
                      AND s.staff_id LIKE 'DOC-%'
                      AND UPPER(TRIM(NVL(s.employment_status, 'ACTIVE'))) = 'ACTIVE'
                )
                ORDER BY d.department_name
                """;

        List<DepartmentDTO> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            DepartmentDTO dto = new DepartmentDTO();
            dto.setDepartmentId(rs.getString("department_id"));
            dto.setDepartmentName(rs.getString("department_name"));
            return dto;
        });

        return ResponseEntity.ok(new ApiResponse<>(true, "진료과 목록 조회 완료", list));
    }
}
