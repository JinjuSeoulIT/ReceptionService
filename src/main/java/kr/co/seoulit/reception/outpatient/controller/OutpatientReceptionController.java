package kr.co.seoulit.reception.outpatient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusHistoryDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusUpdateRequest;
import kr.co.seoulit.reception.outpatient.entity.ReceptionAuditEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionCallHistoryEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionClosureReasonEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionQualificationItemEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionQualificationSnapshotEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionSettlementSnapshotEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionVisitClosureEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionVisitClosureHistoryEntity;
import kr.co.seoulit.reception.outpatient.repository.OutpatientWaitingQueueRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionAuditRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionCallHistoryRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionClosureReasonRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionQualificationItemRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionQualificationSnapshotRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionSettlementSnapshotRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionVisitClosureHistoryRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionVisitClosureRepository;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/receptions")
@Tag(name = "Outpatient Reception", description = "Outpatient reception APIs")
@Slf4j
public class OutpatientReceptionController {

    private final OutpatientReceptionService receptionService;
    private final ReceptionQualificationSnapshotRepository qualificationSnapshotRepository;
    private final ReceptionQualificationItemRepository qualificationItemRepository;
    private final OutpatientWaitingQueueRepository waitingQueueRepository;
    private final ReceptionCallHistoryRepository callHistoryRepository;
    private final ReceptionVisitClosureRepository visitClosureRepository;
    private final ReceptionClosureReasonRepository closureReasonRepository;
    private final ReceptionVisitClosureHistoryRepository visitClosureHistoryRepository;
    private final ReceptionSettlementSnapshotRepository settlementSnapshotRepository;
    private final ReceptionAuditRepository receptionAuditRepository;

    @Operation(summary = "Get outpatient receptions")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OutpatientReceptionDTO>>> getReceptions(
            @Parameter(description = "Search type") @RequestParam(required = false) String searchType,
            @Parameter(description = "Search value") @RequestParam(required = false) String searchValue,
            @Parameter(description = "Date from (YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "Date to (YYYY-MM-DD)") @RequestParam(required = false) String dateTo,
            @Parameter(description = "Department id") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Doctor id") @RequestParam(required = false) Long doctorId
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

    @Operation(summary = "Get outpatient reception queue")
    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<OutpatientReceptionDTO>>> getReceptionQueue(
            @Parameter(description = "Department id") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Doctor id") @RequestParam(required = false) Long doctorId,
            @Parameter(description = "Date (YYYY-MM-DD)") @RequestParam(required = false) String date
    ) {
        List<OutpatientReceptionDTO> list = receptionService.getReceptionQueue(departmentId, doctorId, date);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception queue fetched", list));
    }

    @Operation(summary = "Get outpatient reception detail")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OutpatientReceptionDTO>> getReception(
            @Parameter(description = "Reception id") @PathVariable Long id
    ) {
        log.info("Get reception request: id={}", id);
        OutpatientReceptionDTO dto = receptionService.getReception(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception fetched", dto));
    }

    @Operation(summary = "Create outpatient reception")
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> createReception(@RequestBody OutpatientReceptionDTO reception) {
        log.info("Create reception request: receptionNo={}", reception.getReceptionNo());
        receptionService.createReception(reception);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception created", true));
    }

    @Operation(summary = "Update outpatient reception")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateReception(
            @Parameter(description = "Reception id") @PathVariable Long id,
            @RequestBody OutpatientReceptionDTO reception
    ) {
        log.info("Update reception request: id={}", id);
        receptionService.updateReception(id, reception);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception updated", true));
    }

    @Operation(summary = "Cancel outpatient reception")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> cancelReception(
            @Parameter(description = "Reception id") @PathVariable Long id,
            @Parameter(description = "User id") @RequestParam(required = false) Long changedBy,
            @Parameter(description = "Reason code") @RequestParam(required = false) String reasonCode,
            @Parameter(description = "Reason text") @RequestParam(required = false) String reasonText
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

    @Operation(summary = "Update outpatient reception status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OutpatientReceptionDTO>> updateReceptionStatus(
            @Parameter(description = "Reception id") @PathVariable Long id,
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

    @Operation(summary = "Get outpatient reception status history")
    @GetMapping("/{id}/status-history")
    public ResponseEntity<ApiResponse<List<OutpatientReceptionStatusHistoryDTO>>> getStatusHistory(
            @Parameter(description = "Reception id") @PathVariable Long id
    ) {
        List<OutpatientReceptionStatusHistoryDTO> list = receptionService.getReceptionStatusHistory(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception status history fetched", list));
    }

    @Operation(summary = "Get qualification snapshots")
    @GetMapping("/{id}/qualification-snapshots")
    public ResponseEntity<ApiResponse<List<ReceptionQualificationSnapshotEntity>>> getQualificationSnapshots(
            @Parameter(description = "Reception id") @PathVariable Long id
    ) {
        List<ReceptionQualificationSnapshotEntity> list =
                qualificationSnapshotRepository.findByReceptionIdOrderBySnapshotDatetimeDesc(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Qualification snapshots fetched", list));
    }

    @Operation(summary = "Get qualification items")
    @GetMapping("/{id}/qualification-items")
    public ResponseEntity<ApiResponse<List<ReceptionQualificationItemEntity>>> getQualificationItems(
            @Parameter(description = "Reception id") @PathVariable Long id
    ) {
        List<ReceptionQualificationSnapshotEntity> snapshots =
                qualificationSnapshotRepository.findByReceptionIdOrderBySnapshotDatetimeDesc(id);
        if (snapshots.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Qualification items fetched", Collections.emptyList()));
        }

        List<ReceptionQualificationItemEntity> items = new ArrayList<>();
        for (ReceptionQualificationSnapshotEntity snapshot : snapshots) {
            items.addAll(
                    qualificationItemRepository.findByQualificationSnapshotIdOrderByDisplayOrderAsc(
                            snapshot.getQualificationSnapshotId()
                    )
            );
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "Qualification items fetched", items));
    }

    @Operation(summary = "Get call history")
    @GetMapping("/{id}/call-history")
    public ResponseEntity<ApiResponse<List<ReceptionCallHistoryEntity>>> getCallHistory(
            @Parameter(description = "Reception id") @PathVariable Long id
    ) {
        List<ReceptionCallHistoryEntity> list = waitingQueueRepository.findByReceptionId(id)
                .map(queue -> callHistoryRepository.findByWaitingQueueIdOrderByCallDatetimeDesc(queue.getWaitingQueueId()))
                .orElseGet(Collections::emptyList);
        return ResponseEntity.ok(new ApiResponse<>(true, "Call history fetched", list));
    }

    @Operation(summary = "Get visit closure")
    @GetMapping("/{id}/visit-closure")
    public ResponseEntity<ApiResponse<ReceptionVisitClosureEntity>> getVisitClosure(
            @Parameter(description = "Reception id") @PathVariable Long id
    ) {
        ReceptionVisitClosureEntity closure = visitClosureRepository.findByReceptionId(id).orElse(null);
        return ResponseEntity.ok(new ApiResponse<>(true, "Visit closure fetched", closure));
    }

    @Operation(summary = "Get closure reason code list")
    @GetMapping("/closure-reasons")
    public ResponseEntity<ApiResponse<List<ReceptionClosureReasonEntity>>> getClosureReasons() {
        List<ReceptionClosureReasonEntity> reasons = closureReasonRepository.findByUsableYnOrderBySortOrderAsc("Y");
        return ResponseEntity.ok(new ApiResponse<>(true, "Closure reasons fetched", reasons));
    }

    @Operation(summary = "Get visit closure history")
    @GetMapping("/{id}/visit-closure-history")
    public ResponseEntity<ApiResponse<List<ReceptionVisitClosureHistoryEntity>>> getVisitClosureHistory(
            @Parameter(description = "Reception id") @PathVariable Long id
    ) {
        List<ReceptionVisitClosureHistoryEntity> list = visitClosureRepository.findByReceptionId(id)
                .map(closure -> visitClosureHistoryRepository.findByVisitClosureIdOrderByChangedAtDesc(closure.getVisitClosureId()))
                .orElseGet(Collections::emptyList);
        return ResponseEntity.ok(new ApiResponse<>(true, "Visit closure history fetched", list));
    }

    @Operation(summary = "Get settlement snapshots")
    @GetMapping("/{id}/settlement-snapshots")
    public ResponseEntity<ApiResponse<List<ReceptionSettlementSnapshotEntity>>> getSettlementSnapshots(
            @Parameter(description = "Reception id") @PathVariable Long id
    ) {
        List<ReceptionSettlementSnapshotEntity> list =
                settlementSnapshotRepository.findByReceptionIdOrderBySnapshotDatetimeDesc(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Settlement snapshots fetched", list));
    }

    @Operation(summary = "Get reception audits")
    @GetMapping("/{id}/audits")
    public ResponseEntity<ApiResponse<List<ReceptionAuditEntity>>> getReceptionAudits(
            @Parameter(description = "Reception id") @PathVariable Long id
    ) {
        List<ReceptionAuditEntity> list = receptionAuditRepository.findByReceptionIdOrderByChangedAtDesc(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reception audits fetched", list));
    }
}
