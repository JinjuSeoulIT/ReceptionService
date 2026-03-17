package kr.co.seoulit.common.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "감사 로그 정보")
@Data
public class AuditLogDTO {

    @Schema(description = "감사 로그 ID")
    private Long auditLogId;

    @Schema(description = "엔티티 유형")
    private String entityType;

    @Schema(description = "엔티티 ID")
    private Long entityId;

    @Schema(description = "작업 유형")
    private String action;

    @Schema(description = "작업자 식별자")
    private Long actorId;

    @Schema(description = "발생 일시")
    private LocalDateTime occurredAt;

    @Schema(description = "사유 코드")
    private String reasonCode;

    @Schema(description = "사유")
    private String reasonText;

    @Schema(description = "변경 전 JSON")
    private String beforeJson;

    @Schema(description = "변경 후 JSON")
    private String afterJson;

    @Schema(description = "변경 차이 JSON")
    private String diffJson;
}
