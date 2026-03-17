package kr.co.seoulit.reception.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusHistoryDTO;
import kr.co.seoulit.reception.service.ReceptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reception")
@Tag(name = "접수 이력 API", description = "접수 이력 API")
public class ReceptionHistoryController {

    private final ReceptionService receptionService;

    @Operation(summary = "접수 변경 이력 조회", description = "접수 변경 이력 조회")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getReceptionHistory() {
        List<OutpatientReceptionDTO> receptions = receptionService.getReceptionList(new HashMap<>());
        List<Map<String, Object>> result = receptions.stream()
                .flatMap(reception -> receptionService.getReceptionStatusHistory(reception.getReceptionId())
                        .stream()
                        .map(history -> toHistoryRow(reception, history)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "접수 변경 이력 조회 완료", result));
    }

    private Map<String, Object> toHistoryRow(OutpatientReceptionDTO reception, OutpatientReceptionStatusHistoryDTO history) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", history.getStatusHistoryId());
        row.put("visitId", reception.getReceptionId());
        row.put("eventType", "STATUS_CHANGE");
        row.put("fieldName", "status");
        row.put("oldValue", history.getFromStatus());
        row.put("newValue", history.getToStatus());
        row.put("reason", history.getReasonText());
        row.put("changedBy", history.getChangedBy());
        row.put("changedAt", history.getChangedAt());
        return row;
    }
}
