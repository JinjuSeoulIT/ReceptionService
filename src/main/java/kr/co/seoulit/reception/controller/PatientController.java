package kr.co.seoulit.reception.controller;

import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.dto.PatientDTO;
import kr.co.seoulit.reception.entity.PatientEntity;
import kr.co.seoulit.reception.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientRepository patientRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientDTO>>> getPatients() {
        List<PatientDTO> list = patientRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "환자 목록 조회 완료", list));
    }

    private PatientDTO toDto(PatientEntity entity) {
        PatientDTO dto = new PatientDTO();
        dto.setPatientId(entity.getPatientId());
        dto.setPatientName(entity.getPatientName());
        return dto;
    }
}
