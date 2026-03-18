package kr.co.seoulit.reception.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "Reservation status update request")
@Data
public class ReservationReceptionStatusUpdateRequest {

    @Schema(description = "Status")
    @NotBlank(message = "status is required")
    private String status;

    @Schema(description = "Changed by")
    private Long changedBy;

    @Schema(description = "Reason code")
    private String reasonCode;

    @Schema(description = "Reason text")
    private String reasonText;
}
