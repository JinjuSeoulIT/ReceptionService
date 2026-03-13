package kr.co.seoulit.reception.outpatient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.seoulit.reception.common.api.ApiResponse;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusHistoryDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusUpdateRequest;
import kr.co.seoulit.reception.outpatient.service.OutpatientReceptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/receptions")
@Tag(name = "외래 접수", description = "외래 접수 기능")
@Slf4j
public class OutpatientReceptionController {

    private final OutpatientReceptionService receptionService;

    @Operation(summary = "접수 목록 조회", description = "외래 접수 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OutpatientReceptionDTO>>> getReceptions(
            @Parameter(description = "검색 구분") @RequestParam(required = false) String searchType,
            @Parameter(description = "검색어") @RequestParam(required = false) String searchValue,
            @Parameter(description = "시작일 (연-월-일)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "종료일 (연-월-일)") @RequestParam(required = false) String dateTo,
            @Parameter(description = "진료과 식별자") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "의사 식별자") @RequestParam(required = false) Long doctorId
    ) {
        log.info("Get receptions request: searchType={}, searchValue={}", searchType, searchValue);
        HashMap<String, Object> searchCondition = new HashMap<>();
        searchCondition.put("searchType", searchType);
        searchCondition.put("searchValue", searchValue);
        searchCondition.put("dateFrom", dateFrom);
        searchCondition.put("dateTo", dateTo);
        searchCondition.put("departmentId", departmentId);
        searchCondition.put("doctorId", doctorId);

        List<OutpatientReceptionDTO> list = receptionService.getReceptionList(searchCondition);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception list fetched", list));
    }

    @Operation(summary = "접수 대기열 조회", description = "진료과/의사/일자 기준 대기열을 조회합니다.")
    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<OutpatientReceptionDTO>>> getReceptionQueue(
            @Parameter(description = "진료과 식별자") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "의사 식별자") @RequestParam(required = false) Long doctorId,
            @Parameter(description = "일자 (연-월-일)") @RequestParam(required = false) String date
    ) {
        List<OutpatientReceptionDTO> list = receptionService.getReceptionQueue(departmentId, doctorId, date);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception queue fetched", list));
    }

    @Operation(summary = "접수 상세 조회", description = "접수 상세 1건을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OutpatientReceptionDTO>> getReception(
            @Parameter(description = "접수 식별자") @PathVariable Long id
    ) {
        log.info("Get reception request: id={}", id);
        OutpatientReceptionDTO dto = receptionService.getReception(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception fetched", dto));
    }

    @Operation(summary = "접수 등록", description = "외래 접수를 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> createReception(@RequestBody OutpatientReceptionDTO reception) {
        log.info("Create reception request: receptionNo={}", reception.getReceptionNo());
        receptionService.createReception(reception);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception created", true));
    }

    @Operation(summary = "접수 수정", description = "외래 접수를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateReception(
            @Parameter(description = "접수 식별자") @PathVariable Long id,
            @RequestBody OutpatientReceptionDTO reception
    ) {
        log.info("Update reception request: id={}", id);
        receptionService.updateReception(id, reception);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception updated", true));
    }

    @Operation(summary = "접수 취소", description = "외래 접수를 취소 처리합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> cancelReception(
            @Parameter(description = "접수 식별자") @PathVariable Long id,
            @Parameter(description = "변경자 식별자") @RequestParam(required = false) Long changedBy,
            @Parameter(description = "사유 코드") @RequestParam(required = false) String reasonCode,
            @Parameter(description = "사유 내용") @RequestParam(required = false) String reasonText
    ) {
        log.info("Cancel reception request: id={}", id);
        receptionService.updateReceptionStatus(
                id,
                "CANCELLED",
                changedBy,
                reasonCode != null ? reasonCode : "USER_CANCEL",
                reasonText != null ? reasonText : "Cancelled from frontend"
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception cancelled", true));
    }

    @Operation(summary = "접수 상태 변경", description = "외래 접수 상태를 변경합니다.")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OutpatientReceptionDTO>> updateReceptionStatus(
            @Parameter(description = "접수 식별자") @PathVariable Long id,
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
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception status updated", updated));
    }

    @Operation(summary = "상태 이력 조회", description = "외래 접수 상태 변경 이력을 조회합니다.")
    @GetMapping("/{id}/status-history")
    public ResponseEntity<ApiResponse<List<OutpatientReceptionStatusHistoryDTO>>> getStatusHistory(
            @Parameter(description = "접수 식별자") @PathVariable Long id
    ) {
        List<OutpatientReceptionStatusHistoryDTO> list = receptionService.getReceptionStatusHistory(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception status history fetched", list));
    }
}


