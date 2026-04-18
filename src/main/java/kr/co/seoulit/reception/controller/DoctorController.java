package kr.co.seoulit.reception.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.dto.DoctorDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doctors")
@Tag(name = "의사 API", description = "의사 API")
public class DoctorController {

    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "의사 목록 조회", description = "CMH.STAFF 기준 의사 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorDTO>>> getDoctors(
            @Parameter(description = "진료과 ID")
            @RequestParam(required = false) String departmentId
    ) {
        String normalizedDepartmentId = departmentId == null ? null : departmentId.trim();

        StringBuilder sql = new StringBuilder("""
                SELECT
                    s.staff_id,
                    s.full_name,
                    s.staff_department_id
                FROM CMH.STAFF s
                INNER JOIN CMH.STAFF_DEPARTMENT d
                    ON d.department_id = s.staff_department_id
                WHERE s.staff_id LIKE 'DOC-%'
                  AND UPPER(TRIM(NVL(s.employment_status, 'ACTIVE'))) = 'ACTIVE'
                """);

        List<Object> params = new ArrayList<>();

        if (normalizedDepartmentId != null && !normalizedDepartmentId.isBlank()) {
            sql.append(" AND s.staff_department_id = ? ");
            params.add(normalizedDepartmentId);
        }

        sql.append(" ORDER BY d.department_name, s.full_name ");

        List<DoctorDTO> list = jdbcTemplate.query(
                sql.toString(),
                params.toArray(),
                (rs, rowNum) -> {
                    DoctorDTO dto = new DoctorDTO();
                    dto.setDoctorId(rs.getString("staff_id"));
                    dto.setDoctorName(rs.getString("full_name"));
                    dto.setDepartmentId(rs.getString("staff_department_id"));
                    return dto;
                }
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "의사 목록 조회 완료", list));
    }
}
