package kr.co.seoulit.reception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Reception status history")
@Data
public class ReceptionStatusHistoryDTO {

    @Schema(description = "Status history ID")
    private Long statusHistoryId;

    @Schema(description = "Reception ID")
    private Long receptionId;

    @Schema(description = "From status")
    private String fromStatus;

    @Schema(description = "To status")
    private String toStatus;

    @Schema(description = "Changed by")
    private Long changedBy;

    @Schema(description = "Changed at")
    private LocalDateTime changedAt;

    @Schema(description = "Reason code")
    private String reasonCode;

    @Schema(description = "Reason text")
    private String reasonText;
}
