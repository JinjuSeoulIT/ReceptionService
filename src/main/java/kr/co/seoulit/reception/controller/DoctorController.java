package kr.co.seoulit.reception.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.dto.DoctorDTO;
import kr.co.seoulit.reception.entity.DoctorEntity;
import kr.co.seoulit.reception.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doctors")
@Tag(name = "의사 API", description = "의사 API")
public class DoctorController {

    private final DoctorRepository doctorRepository;

    @Operation(summary = "의사 목록 조회", description = "의사 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorDTO>>> getDoctors(
            @Parameter(description = "진료과 ID")
            @RequestParam(required = false) Long departmentId
    ) {
        List<DoctorEntity> entities = departmentId == null
                ? doctorRepository.findAll()
                : doctorRepository.findByDepartmentId(departmentId);

        List<DoctorDTO> list = entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "의사 목록 조회 완료", list));
    }

    private DoctorDTO toDto(DoctorEntity entity) {
        DoctorDTO dto = new DoctorDTO();
        dto.setDoctorId(entity.getDoctorId());
        dto.setDoctorName(entity.getDoctorName());
        dto.setDepartmentId(entity.getDepartmentId());
        return dto;
    }
}
