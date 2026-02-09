package kr.co.seoulit.common.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.seoulit.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audit-logs")
@Tag(name = "감사 로그 API", description = "감사 로그 API")
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Operation(summary = "감사 로그 목록 조회", description = "엔티티 또는 처리자 기준 감사 로그 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLogDTO>>> getAuditLogs(
            @Parameter(description = "엔티티 유형") @RequestParam(required = false) String entityType,
            @Parameter(description = "엔티티 ID") @RequestParam(required = false) Long entityId,
            @Parameter(description = "처리자 ID") @RequestParam(required = false) Long actorId,
            @Parameter(description = "조회 시작 시간") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "조회 종료 시간") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        if (entityType != null && entityId != null) {
            List<AuditLogDTO> list = auditLogService.getByEntity(entityType, entityId);
            return ResponseEntity.ok(new ApiResponse<>(true, "감사 로그 조회 완료", list));
        }
        if (actorId != null && from != null && to != null) {
            List<AuditLogDTO> list = auditLogService.getByActor(actorId, from, to);
            return ResponseEntity.ok(new ApiResponse<>(true, "감사 로그 조회 완료", list));
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "감사 로그 조회 완료", Collections.emptyList()));
    }
}
