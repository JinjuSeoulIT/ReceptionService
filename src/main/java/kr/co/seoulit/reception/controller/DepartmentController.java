package kr.co.seoulit.reception.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.dto.DepartmentDTO;
import kr.co.seoulit.reception.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/departments")
@Tag(name = "진료과 API", description = "진료과 API")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "진료과 목록 조회", description = "CMH.STAFF_DEPARTMENT 기준 진료과 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getDepartments() {
        List<DepartmentDTO> list = departmentService.getActiveDepartments();
        return ResponseEntity.ok(new ApiResponse<>(true, "진료과 목록 조회 완료", list));
    }
}
