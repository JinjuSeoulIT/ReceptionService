package kr.co.seoulit.reception.outpatient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "외래 접수 상태 변경 실시간 이벤트")
@Data
public class OutpatientReceptionStatusChangedEventDTO {

    @Schema(description = "접수 ID")
    private Long receptionId;

    @Schema(description = "접수 번호")
    private String receptionNo;

    @Schema(description = "환자 ID")
    private Long patientId;

    @Schema(description = "환자명")
    private String patientName;

    @Schema(description = "이전 상태")
    private String fromStatus;

    @Schema(description = "변경 상태")
    private String toStatus;

    @Schema(description = "변경 시간")
    private LocalDateTime changedAt;
}
