package kr.co.seoulit.reception.emergency.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.emergency.dto.EmergencyReceptionDTO;
import kr.co.seoulit.reception.emergency.service.EmergencyReceptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emergency-receptions")
@Tag(name = "응급 접수", description = "응급 접수 기능")
@Slf4j
public class EmergencyReceptionController {

    private final EmergencyReceptionService emergencyReceptionService;

    @Operation(summary = "응급 접수 목록 조회", description = "응급 접수 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmergencyReceptionDTO>>> getEmergencyReceptions(
            @Parameter(description = "검색 구분") @RequestParam(required = false) String searchType,
            @Parameter(description = "검색어") @RequestParam(required = false) String searchValue
    ) {
        log.info("응급 접수 목록 요청: searchType={}, searchValue={}", searchType, searchValue);
        HashMap<String, Object> searchCondition = new HashMap<>();
        searchCondition.put("searchType", searchType);
        searchCondition.put("searchValue", searchValue);

        List<EmergencyReceptionDTO> list = emergencyReceptionService.getEmergencyReceptionList(searchCondition);
        return ResponseEntity.ok(new ApiResponse<>(true, "응급 접수 목록 조회 완료", list));
    }

    @Operation(summary = "응급 접수 상세 조회", description = "응급 접수 상세 1건을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmergencyReceptionDTO>> getEmergencyReception(
            @Parameter(description = "접수 식별자") @PathVariable Long id
    ) {
        log.info("응급 접수 조회 요청: id={}", id);
        EmergencyReceptionDTO dto = emergencyReceptionService.getEmergencyReception(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "응급 접수 조회 완료", dto));
    }

    @Operation(summary = "응급 접수 등록", description = "응급 접수를 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> createEmergencyReception(@RequestBody EmergencyReceptionDTO request) {
        log.info("응급 Create reception request: receptionNo={}", request.getReceptionNo());
        emergencyReceptionService.createEmergencyReception(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Emergency reception created", true));
    }

    @Operation(summary = "응급 접수 수정", description = "응급 접수를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateEmergencyReception(
            @Parameter(description = "접수 식별자") @PathVariable Long id,
            @RequestBody EmergencyReceptionDTO request
    ) {
        log.info("응급 Update reception request: id={}", id);
        emergencyReceptionService.updateEmergencyReception(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Emergency reception updated", true));
    }

    @Operation(summary = "응급 접수 취소", description = "응급 접수를 취소 처리합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> cancelEmergencyReception(
            @Parameter(description = "접수 식별자") @PathVariable Long id
    ) {
        log.info("응급 Cancel reception request: id={}", id);
        EmergencyReceptionDTO request = new EmergencyReceptionDTO();
        request.setStatus("CANCELLED");
        request.setIsActive(false);
        request.setNote("Cancelled from frontend");
        emergencyReceptionService.updateEmergencyReception(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "응급 접수 수정 완료", true));
    }
}


