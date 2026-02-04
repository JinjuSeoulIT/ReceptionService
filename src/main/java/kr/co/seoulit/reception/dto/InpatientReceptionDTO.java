package kr.co.seoulit.reception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "?? ?? ??")
@Data
public class InpatientReceptionDTO implements Serializable {

    @Schema(description = "?? ID")
    private Long receptionId;

    @Schema(description = "????")
    private String receptionNo;

    @Schema(description = "?? ID")
    private Long patientId;

    @Schema(description = "?? ??")
    private String patientName;

    @Schema(description = "?? ??")
    private String visitType;

    @Schema(description = "??? ID")
    private Long departmentId;

    @Schema(description = "??? ??")
    private String departmentName;

    @Schema(description = "?? ID")
    private Long doctorId;

    @Schema(description = "?? ??")
    private String doctorName;

    @Schema(description = "?? ID")
    private Long reservationId;

    @Schema(description = "?? ??")
    private LocalDateTime scheduledAt;

    @Schema(description = "?? ??")
    private LocalDateTime arrivedAt;

    @Schema(description = "??")
    private String status;

    @Schema(description = "??")
    private String note;

    @Schema(description = "?? ??")
    private Boolean isActive;

    @Schema(description = "?? ?? ??")
    private LocalDateTime admissionPlanAt;

    @Schema(description = "?? ID")
    private Long wardId;

    @Schema(description = "?? ID")
    private Long roomId;

    @Schema(description = "?? ??")
    private LocalDateTime createdAt;

    @Schema(description = "?? ??")
    private LocalDateTime updatedAt;
}
