package kr.co.seoulit.reception.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "예약 상태 변경 요청")
@Data
public class ReservationReceptionStatusUpdateRequest {

    @Schema(description = "상태")
    private String status;

    @Schema(description = "변경자 식별자")
    private Long changedBy;

    @Schema(description = "사유 코드")
    private String reasonCode;

    @Schema(description = "사유")
    private String reasonText;
}