package kr.co.seoulit.reception.outpatient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.outpatient.dto.OutpatientCallHistoryDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientClosureReasonDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionAuditDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientQualificationItemDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientQualificationSnapshotDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusHistoryDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusUpdateRequest;
import kr.co.seoulit.reception.outpatient.dto.OutpatientSettlementSnapshotDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientVisitClosureDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientVisitClosureHistoryDTO;
import kr.co.seoulit.reception.outpatient.realtime.OutpatientReceptionStatusEventPublisher;
import kr.co.seoulit.reception.outpatient.service.OutpatientReceptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/receptions")
@Tag(name = "외래 접수", description = "외래 접수 API")
@Slf4j
@Validated
public class OutpatientReceptionController {

    private final OutpatientReceptionService receptionService;
    private final OutpatientReceptionStatusEventPublisher receptionStatusEventPublisher;

    @Operation(summary = "외래 접수 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OutpatientReceptionDTO>>> getReceptions(
            @Parameter(description = "검색 유형") @RequestParam(required = false) String searchType,
            @Parameter(description = "검색어") @RequestParam(required = false) String searchValue,
            @Parameter(description = "시작일 (YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "종료일 (YYYY-MM-DD)") @RequestParam(required = false) String dateTo,
            @Parameter(description = "진료과 ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "의사 ID") @RequestParam(required = false) Long doctorId
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

    @Operation(summary = "외래 접수 상태 이벤트 구독")
    @GetMapping(value = "/events/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamReceptionStatusEvents() {
        log.info("Subscribe reception status events");
        return receptionStatusEventPublisher.subscribe();
    }

    @Operation(summary = "외래 접수 대기열 조회")
    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<OutpatientReceptionDTO>>> getReceptionQueue(
            @Parameter(description = "진료과 ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "의사 ID") @RequestParam(required = false) Long doctorId,
            @Parameter(description = "조회일 (YYYY-MM-DD)") @RequestParam(required = false) String date
    ) {
        List<OutpatientReceptionDTO> list = receptionService.getReceptionQueue(departmentId, doctorId, date);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception queue fetched", list));
    }

    @Operation(summary = "외래 접수 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OutpatientReceptionDTO>> getReception(
            @Parameter(description = "접수 ID") @PathVariable Long id
    ) {
        log.info("Get reception request: id={}", id);
        OutpatientReceptionDTO dto = receptionService.getReception(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception fetched", dto));
    }

    @Operation(summary = "외래 접수 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> createReception(@Valid @RequestBody OutpatientReceptionDTO reception) {
        log.info("Create reception request: receptionNo={}", reception.getReceptionNo());
        receptionService.createReception(reception);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception created", true));
    }

    @Operation(summary = "외래 접수 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateReception(
            @Parameter(description = "접수 ID") @PathVariable Long id,
            @Valid @RequestBody OutpatientReceptionDTO reception
    ) {
        log.info("Update reception request: id={}", id);
        receptionService.updateReception(id, reception);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception updated", true));
    }

    @Operation(summary = "외래 접수 취소")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> cancelReception(
            @Parameter(description = "접수 ID") @PathVariable Long id,
            @Parameter(description = "사용자 ID") @RequestParam(required = false) Long changedBy,
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

    @Operation(summary = "외래 접수 상태 변경")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OutpatientReceptionDTO>> updateReceptionStatus(
            @Parameter(description = "접수 ID") @PathVariable Long id,
            @Valid @RequestBody OutpatientReceptionStatusUpdateRequest request
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

    @Operation(summary = "외래 접수 상태 이력 조회")
    @GetMapping("/{id}/status-history")
    public ResponseEntity<ApiResponse<List<OutpatientReceptionStatusHistoryDTO>>> getStatusHistory(
            @Parameter(description = "접수 ID") @PathVariable Long id
    ) {
        List<OutpatientReceptionStatusHistoryDTO> list = receptionService.getReceptionStatusHistory(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception status history fetched", list));
    }

    @Operation(summary = "수진자격 스냅샷 조회")
    @GetMapping("/{id}/qualification-snapshots")
    public ResponseEntity<ApiResponse<List<OutpatientQualificationSnapshotDTO>>> getQualificationSnapshots(
            @Parameter(description = "접수 ID") @PathVariable Long id
    ) {
        List<OutpatientQualificationSnapshotDTO> list = receptionService.getQualificationSnapshots(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Qualification snapshots fetched", list));
    }

    @Operation(summary = "수진자격 항목 조회")
    @GetMapping("/{id}/qualification-items")
    public ResponseEntity<ApiResponse<List<OutpatientQualificationItemDTO>>> getQualificationItems(
            @Parameter(description = "접수 ID") @PathVariable Long id
    ) {
        List<OutpatientQualificationItemDTO> items = receptionService.getLatestQualificationItems(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Qualification items fetched", items));
    }

    @Operation(summary = "호출 이력 조회")
    @GetMapping("/{id}/call-history")
    public ResponseEntity<ApiResponse<List<OutpatientCallHistoryDTO>>> getCallHistory(
            @Parameter(description = "접수 ID") @PathVariable Long id
    ) {
        List<OutpatientCallHistoryDTO> list = receptionService.getCallHistory(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Call history fetched", list));
    }

    @Operation(summary = "진료 종료 정보 조회")
    @GetMapping("/{id}/visit-closure")
    public ResponseEntity<ApiResponse<OutpatientVisitClosureDTO>> getVisitClosure(
            @Parameter(description = "접수 ID") @PathVariable Long id
    ) {
        OutpatientVisitClosureDTO closure = receptionService.getVisitClosure(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Visit closure fetched", closure));
    }

    @Operation(summary = "진료 종료 사유 코드 목록 조회")
    @GetMapping("/closure-reasons")
    public ResponseEntity<ApiResponse<List<OutpatientClosureReasonDTO>>> getClosureReasons() {
        List<OutpatientClosureReasonDTO> reasons = receptionService.getClosureReasons();
        return ResponseEntity.ok(new ApiResponse<>(true, "Closure reasons fetched", reasons));
    }

    @Operation(summary = "진료 종료 이력 조회")
    @GetMapping("/{id}/visit-closure-history")
    public ResponseEntity<ApiResponse<List<OutpatientVisitClosureHistoryDTO>>> getVisitClosureHistory(
            @Parameter(description = "접수 ID") @PathVariable Long id
    ) {
        List<OutpatientVisitClosureHistoryDTO> list = receptionService.getVisitClosureHistory(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Visit closure history fetched", list));
    }

    @Operation(summary = "정산 스냅샷 조회")
    @GetMapping("/{id}/settlement-snapshots")
    public ResponseEntity<ApiResponse<List<OutpatientSettlementSnapshotDTO>>> getSettlementSnapshots(
            @Parameter(description = "접수 ID") @PathVariable Long id
    ) {
        List<OutpatientSettlementSnapshotDTO> list = receptionService.getSettlementSnapshots(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Settlement snapshots fetched", list));
    }

    @Operation(summary = "접수 감사 이력 조회")
    @GetMapping("/{id}/audits")
    public ResponseEntity<ApiResponse<List<OutpatientReceptionAuditDTO>>> getReceptionAudits(
            @Parameter(description = "접수 ID") @PathVariable Long id
    ) {
        List<OutpatientReceptionAuditDTO> list = receptionService.getReceptionAudits(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception audits fetched", list));
    }
}
