package kr.co.seoulit.reception.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusHistoryDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusUpdateRequest;
import kr.co.seoulit.reception.service.ReceptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/receptions")
@Tag(name = "외래 접수 API", description = "외래 접수 API")
@Slf4j
public class ReceptionController {

    private final ReceptionService receptionService;

    @Operation(summary = "외래 접수 목록 조회", description = "외래 접수 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OutpatientReceptionDTO>>> getReceptions(
            @Parameter(description = "검색 타입") @RequestParam(required = false) String searchType,
            @Parameter(description = "검색 값") @RequestParam(required = false) String searchValue,
            @Parameter(description = "시작일 YYYY-MM-DD") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "종료일 YYYY-MM-DD") @RequestParam(required = false) String dateTo,
            @Parameter(description = "진료과 ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "의사 ID") @RequestParam(required = false) Long doctorId
    ) {
        log.info("Reception list request: searchType={}, searchValue={}", searchType, searchValue);
        HashMap<String, Object> searchCondition = new HashMap<>();
        searchCondition.put("searchType", searchType);
        searchCondition.put("searchValue", searchValue);
        searchCondition.put("dateFrom", dateFrom);
        searchCondition.put("dateTo", dateTo);
        searchCondition.put("departmentId", departmentId);
        searchCondition.put("doctorId", doctorId);

        List<OutpatientReceptionDTO> list = receptionService.getReceptionList(searchCondition);
        return ResponseEntity.ok(new ApiResponse<>(true, "외래 접수 목록 조회 완료", list));
    }

    @Operation(summary = "외래 접수 대기열 조회", description = "외래 접수 대기열 조회")
    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<OutpatientReceptionDTO>>> getReceptionQueue(
            @Parameter(description = "진료과 ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "의사 ID") @RequestParam(required = false) Long doctorId,
            @Parameter(description = "조회일 YYYY-MM-DD") @RequestParam(required = false) String date
    ) {
        List<OutpatientReceptionDTO> list = receptionService.getReceptionQueue(departmentId, doctorId, date);
        return ResponseEntity.ok(new ApiResponse<>(true, "외래 접수 대기열 조회 완료", list));
    }

    @Operation(summary = "외래 접수 상세 조회", description = "외래 접수 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OutpatientReceptionDTO>> getReception(
            @Parameter(description = "접수 ID") @PathVariable Long id
    ) {
        log.info("Get reception request: id={}", id);
        OutpatientReceptionDTO dto = receptionService.getReception(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "외래 접수 상세 조회 완료", dto));
    }

    @Operation(summary = "외래 접수 등록", description = "외래 접수 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> createReception(@RequestBody OutpatientReceptionDTO reception) {
        log.info("Create reception request: receptionNo={}", reception.getReceptionNo());
        receptionService.createReception(reception);
        return ResponseEntity.ok(new ApiResponse<>(true, "외래 접수 등록 완료", true));
    }

    @Operation(summary = "외래 접수 수정", description = "외래 접수 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateReception(
            @Parameter(description = "접수 ID") @PathVariable Long id,
            @RequestBody OutpatientReceptionDTO reception
    ) {
        log.info("Update reception request: id={}", id);
        receptionService.updateReception(id, reception);
        return ResponseEntity.ok(new ApiResponse<>(true, "외래 접수 수정 완료", true));
    }

    @Operation(summary = "외래 접수 상태 변경", description = "외래 접수 상태 변경")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OutpatientReceptionDTO>> updateReceptionStatus(
            @Parameter(description = "접수 ID") @PathVariable Long id,
            @RequestBody OutpatientReceptionStatusUpdateRequest request
    ) {
        log.info("Update reception status request: id={}, status={}", id, request.getStatus());
        OutpatientReceptionDTO updated = receptionService.updateReceptionStatus(
                id,
                request.getStatus(),
                request.getChangedBy(),
                request.getReasonCode(),
                request.getReasonText()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "외래 접수 상태 변경 완료", updated));
    }

    @Operation(summary = "외래 접수 상태 이력 조회", description = "외래 접수 상태 이력 조회")
    @GetMapping("/{id}/status-history")
    public ResponseEntity<ApiResponse<List<OutpatientReceptionStatusHistoryDTO>>> getStatusHistory(
            @Parameter(description = "접수 ID") @PathVariable Long id
    ) {
        List<OutpatientReceptionStatusHistoryDTO> list = receptionService.getReceptionStatusHistory(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "외래 접수 상태 이력 조회 완료", list));
    }
}
