package kr.co.seoulit.common.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Audit log info")
@Data
public class AuditLogDTO {

    @Schema(description = "Audit log ID")
    private Long auditLogId;

    @Schema(description = "Entity type")
    private String entityType;

    @Schema(description = "Entity ID")
    private Long entityId;

    @Schema(description = "Action")
    private String action;

    @Schema(description = "Actor ID")
    private Long actorId;

    @Schema(description = "Occurred at")
    private LocalDateTime occurredAt;

    @Schema(description = "Reason code")
    private String reasonCode;

    @Schema(description = "Reason text")
    private String reasonText;

    @Schema(description = "Before JSON")
    private String beforeJson;

    @Schema(description = "After JSON")
    private String afterJson;

    @Schema(description = "Diff JSON")
    private String diffJson;
}
