package kr.co.seoulit.reception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "접수 상태 변경 이력")
@Data
public class ReceptionStatusHistoryDTO {

    @Schema(description = "상태 이력 ID")
    private Long statusHistoryId;

    @Schema(description = "접수 ID")
    private Long receptionId;

    @Schema(description = "변경 전 상태")
    private String fromStatus;

    @Schema(description = "변경 후 상태")
    private String toStatus;

    @Schema(description = "변경자")
    private Long changedBy;

    @Schema(description = "변경 시각")
    private LocalDateTime changedAt;

    @Schema(description = "사유 코드")
    private String reasonCode;

    @Schema(description = "사유")
    private String reasonText;
}
