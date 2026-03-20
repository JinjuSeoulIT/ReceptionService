package kr.co.seoulit.reception.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "예약 상태 변경 요청")
@Data
public class ReservationReceptionStatusUpdateRequest {

    @Schema(description = "상태")
    @NotBlank(message = "status is required")
    private String status;

    @Schema(description = "변경자 ID")
    private Long changedBy;

    @Schema(description = "사유 코드")
    private String reasonCode;

    @Schema(description = "사유 내용")
    private String reasonText;
}
