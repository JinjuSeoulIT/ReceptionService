package kr.co.seoulit.reception.outpatient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "외래 상태 이력 정보")
@Data
public class OutpatientReceptionStatusHistoryDTO {

    @Schema(description = "상태 이력 ID")
    private Long statusHistoryId;

    @Schema(description = "접수 ID")
    private Long receptionId;

    @Schema(description = "변경 전 상태")
    private String fromStatus;

    @Schema(description = "변경 후 상태")
    private String toStatus;

    @Schema(description = "변경자 식별자")
    private Long changedBy;

    @Schema(description = "변경 일시")
    private LocalDateTime changedAt;

    @Schema(description = "사유 코드")
    private String reasonCode;

    @Schema(description = "사유")
    private String reasonText;
}
