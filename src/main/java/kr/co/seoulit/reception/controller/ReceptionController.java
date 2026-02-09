package kr.co.seoulit.reception.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.dto.ReceptionDTO;
import kr.co.seoulit.reception.dto.ReceptionDashboardDTO;
import kr.co.seoulit.reception.dto.ReceptionChecklistItemRequest;
import kr.co.seoulit.reception.dto.ReceptionTodayScheduleRequest;
import kr.co.seoulit.reception.dto.ReceptionStatusUpdateRequest;
import kr.co.seoulit.reception.dto.ReceptionStatusHistoryDTO;
import kr.co.seoulit.reception.entity.ReceptionChecklistItemEntity;
import kr.co.seoulit.reception.entity.ReceptionTodayScheduleEntity;
import kr.co.seoulit.reception.repository.ReceptionChecklistItemRepository;
import kr.co.seoulit.reception.repository.ReceptionTodayScheduleRepository;
import kr.co.seoulit.reception.service.ReceptionService;
import kr.co.seoulit.reception.service.ReceptionDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/receptions")
@Tag(name = "접수 API", description = "접수 API")
@Slf4j
public class ReceptionController {

    private final ReceptionService receptionService;
    private final ReceptionDashboardService receptionDashboardService;
    private final ReceptionTodayScheduleRepository receptionTodayScheduleRepository;
    private final ReceptionChecklistItemRepository receptionChecklistItemRepository;

    @Operation(summary = "접수 목록 조회", description = "접수 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReceptionDTO>>> getReceptions(
            @Parameter(description = "검색 유형") @RequestParam(required = false) String searchType,
            @Parameter(description = "검색어") @RequestParam(required = false) String searchValue,
            @Parameter(description = "조회 시작일(YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "조회 종료일(YYYY-MM-DD)") @RequestParam(required = false) String dateTo,
            @Parameter(description = "진료과 ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "의사 ID") @RequestParam(required = false) Long doctorId
    ) {
        log.info("접수 목록 요청: searchType={}, searchValue={}", searchType, searchValue);
        HashMap<String, Object> searchCondition = new HashMap<>();
        searchCondition.put("searchType", searchType);
        searchCondition.put("searchValue", searchValue);
        searchCondition.put("dateFrom", dateFrom);
        searchCondition.put("dateTo", dateTo);
        searchCondition.put("departmentId", departmentId);
        searchCondition.put("doctorId", doctorId);

        List<ReceptionDTO> list = receptionService.getReceptionList(searchCondition);
        return ResponseEntity.ok(new ApiResponse<>(true, "접수 목록 조회 완료", list));
    }

    @Operation(summary = "접수 대기열", description = "진료과/의사 대기열")
    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<ReceptionDTO>>> getReceptionQueue(
            @Parameter(description = "진료과 ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "의사 ID") @RequestParam(required = false) Long doctorId,
            @Parameter(description = "조회일(YYYY-MM-DD)") @RequestParam(required = false) String date
    ) {
        List<ReceptionDTO> list = receptionService.getReceptionQueue(departmentId, doctorId, date);
        return ResponseEntity.ok(new ApiResponse<>(true, "접수 대기열 조회 완료", list));
    }

    @Operation(summary = "접수 대시보드", description = "접수 대시보드 내용 조회")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ReceptionDashboardDTO>> getReceptionDashboard() {
        ReceptionDashboardDTO dto = receptionDashboardService.getDashboard();
        return ResponseEntity.ok(new ApiResponse<>(true, "접수 대시보드 조회 완료", dto));
    }

    @Operation(summary = "일정 목록 조회", description = "접수 대시보드 일정 목록 조회")
    @GetMapping("/dashboard/schedules")
    public ResponseEntity<ApiResponse<List<ReceptionTodayScheduleEntity>>> getSchedules(
            @Parameter(description = "조회일(YYYY-MM-DD)") @RequestParam(required = false) String date
    ) {
        LocalDate target = date == null || date.isBlank() ? LocalDate.now() : LocalDate.parse(date);
        List<ReceptionTodayScheduleEntity> list =
                receptionTodayScheduleRepository.findByScheduleDateOrderBySortOrderAsc(target);
        list.sort(Comparator
                .comparing((ReceptionTodayScheduleEntity e) ->
                        e.getSortOrder() == null ? Integer.MAX_VALUE : e.getSortOrder())
                .thenComparing(ReceptionTodayScheduleEntity::getScheduleId));
        return ResponseEntity.ok(new ApiResponse<>(true, "일정 목록 조회 완료", list));
    }

    @Operation(summary = "일정 등록", description = "접수 대시보드 일정 등록")
    @PostMapping("/dashboard/schedules")
    public ResponseEntity<ApiResponse<ReceptionTodayScheduleEntity>> createSchedule(
            @RequestBody ReceptionTodayScheduleRequest request
    ) {
        ReceptionTodayScheduleEntity entity = new ReceptionTodayScheduleEntity();
        entity.setScheduleDate(request.getScheduleDate() == null ? LocalDate.now() : request.getScheduleDate());
        entity.setTimeLabel(request.getTimeLabel());
        entity.setTitle(request.getTitle());
        entity.setSortOrder(request.getSortOrder());
        ReceptionTodayScheduleEntity saved = receptionTodayScheduleRepository.save(entity);
        return ResponseEntity.ok(new ApiResponse<>(true, "일정 등록 완료", saved));
    }

    @Operation(summary = "일정 수정", description = "접수 대시보드 일정 수정")
    @PutMapping("/dashboard/schedules/{id}")
    public ResponseEntity<ApiResponse<ReceptionTodayScheduleEntity>> updateSchedule(
            @Parameter(description = "일정 ID") @PathVariable Long id,
            @RequestBody ReceptionTodayScheduleRequest request
    ) {
        ReceptionTodayScheduleEntity entity = receptionTodayScheduleRepository.findById(id)
                .orElseThrow();
        if (request.getScheduleDate() != null) {
            entity.setScheduleDate(request.getScheduleDate());
        }
        if (request.getTimeLabel() != null) {
            entity.setTimeLabel(request.getTimeLabel());
        }
        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        ReceptionTodayScheduleEntity saved = receptionTodayScheduleRepository.save(entity);
        return ResponseEntity.ok(new ApiResponse<>(true, "일정 수정 완료", saved));
    }

    @Operation(summary = "일정 삭제", description = "접수 대시보드 일정 삭제")
    @DeleteMapping("/dashboard/schedules/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteSchedule(
            @Parameter(description = "일정 ID") @PathVariable Long id
    ) {
        receptionTodayScheduleRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "일정 삭제 완료", Map.of("id", id)));
    }

    @Operation(summary = "체크리스트 조회", description = "접수 대시보드 체크리스트 조회")
    @GetMapping("/dashboard/checklist")
    public ResponseEntity<ApiResponse<List<ReceptionChecklistItemEntity>>> getChecklist(
            @Parameter(description = "조회일(YYYY-MM-DD)") @RequestParam(required = false) String date
    ) {
        LocalDate target = date == null || date.isBlank() ? LocalDate.now() : LocalDate.parse(date);
        List<ReceptionChecklistItemEntity> list =
                receptionChecklistItemRepository.findByCheckDateOrderBySortOrderAsc(target);
        list.sort(Comparator
                .comparing((ReceptionChecklistItemEntity e) ->
                        e.getSortOrder() == null ? Integer.MAX_VALUE : e.getSortOrder())
                .thenComparing(ReceptionChecklistItemEntity::getChecklistId));
        return ResponseEntity.ok(new ApiResponse<>(true, "체크리스트 조회 완료", list));
    }

    @Operation(summary = "체크리스트 등록", description = "접수 대시보드 체크리스트 등록")
    @PostMapping("/dashboard/checklist")
    public ResponseEntity<ApiResponse<ReceptionChecklistItemEntity>> createChecklist(
            @RequestBody ReceptionChecklistItemRequest request
    ) {
        ReceptionChecklistItemEntity entity = new ReceptionChecklistItemEntity();
        entity.setCheckDate(request.getCheckDate() == null ? LocalDate.now() : request.getCheckDate());
        entity.setLabel(request.getLabel());
        entity.setDone(request.isDone());
        entity.setSortOrder(request.getSortOrder());
        ReceptionChecklistItemEntity saved = receptionChecklistItemRepository.save(entity);
        return ResponseEntity.ok(new ApiResponse<>(true, "체크리스트 등록 완료", saved));
    }

    @Operation(summary = "체크리스트 수정", description = "접수 대시보드 체크리스트 수정")
    @PutMapping("/dashboard/checklist/{id}")
    public ResponseEntity<ApiResponse<ReceptionChecklistItemEntity>> updateChecklist(
            @Parameter(description = "체크리스트 ID") @PathVariable Long id,
            @RequestBody ReceptionChecklistItemRequest request
    ) {
        ReceptionChecklistItemEntity entity = receptionChecklistItemRepository.findById(id)
                .orElseThrow();
        if (request.getCheckDate() != null) {
            entity.setCheckDate(request.getCheckDate());
        }
        if (request.getLabel() != null) {
            entity.setLabel(request.getLabel());
        }
        entity.setDone(request.isDone());
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        ReceptionChecklistItemEntity saved = receptionChecklistItemRepository.save(entity);
        return ResponseEntity.ok(new ApiResponse<>(true, "체크리스트 수정 완료", saved));
    }

    @Operation(summary = "체크리스트 삭제", description = "접수 대시보드 체크리스트 삭제")
    @DeleteMapping("/dashboard/checklist/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteChecklist(
            @Parameter(description = "체크리스트 ID") @PathVariable Long id
    ) {
        receptionChecklistItemRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "체크리스트 삭제 완료", Map.of("id", id)));
    }

    @Operation(summary = "접수 조회", description = "접수 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReceptionDTO>> getReception(
            @Parameter(description = "접수 ID") @PathVariable Long id
    ) {
        log.info("접수 조회 요청: id={}", id);
        ReceptionDTO dto = receptionService.getReception(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "접수 조회 완료", dto));
    }

    @Operation(summary = "접수 등록", description = "접수 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> createReception(@RequestBody ReceptionDTO reception) {
        log.info("접수 등록 요청: receptionNo={}", reception.getReceptionNo());
        receptionService.createReception(reception);
        return ResponseEntity.ok(new ApiResponse<>(true, "접수 등록 완료", true));
    }

    @Operation(summary = "접수 수정", description = "접수 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateReception(
            @Parameter(description = "접수 ID") @PathVariable Long id,
            @RequestBody ReceptionDTO reception
    ) {
        log.info("접수 수정 요청: id={}", id);
        receptionService.updateReception(id, reception);
        return ResponseEntity.ok(new ApiResponse<>(true, "접수 수정 완료", true));
    }

    @Operation(summary = "접수 상태 변경", description = "접수 상태 변경")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ReceptionDTO>> updateReceptionStatus(
            @Parameter(description = "접수 ID") @PathVariable Long id,
            @RequestBody ReceptionStatusUpdateRequest request
    ) {
        log.info("접수 상태 변경 요청: id={}, status={}", id, request.getStatus());
        ReceptionDTO updated = receptionService.updateReceptionStatus(
                id,
                request.getStatus(),
                request.getChangedBy(),
                request.getReasonCode(),
                request.getReasonText()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "접수 상태 변경 완료", updated));
    }

    @Operation(summary = "접수 상태 이력", description = "접수 상태 이력")
    @GetMapping("/{id}/status-history")
    public ResponseEntity<ApiResponse<List<ReceptionStatusHistoryDTO>>> getStatusHistory(
            @Parameter(description = "접수 ID") @PathVariable Long id
    ) {
        List<ReceptionStatusHistoryDTO> list = receptionService.getReceptionStatusHistory(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "접수 상태 이력 조회 완료", list));
    }
}
