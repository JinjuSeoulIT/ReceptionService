package kr.co.seoulit.reception.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.dto.DoctorDTO;
import kr.co.seoulit.reception.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doctors")
@Tag(name = "의사 API", description = "의사 API")
public class DoctorController {

    private final DoctorService doctorService;

    @Operation(summary = "의사 목록 조회", description = "CMH.STAFF 기준 의사 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorDTO>>> getDoctors(
            @Parameter(description = "진료과 ID")
            @RequestParam(required = false) String departmentId
    ) {
        List<DoctorDTO> list = doctorService.getActiveDoctors(departmentId);
        return ResponseEntity.ok(new ApiResponse<>(true, "의사 목록 조회 완료", list));
    }
}
