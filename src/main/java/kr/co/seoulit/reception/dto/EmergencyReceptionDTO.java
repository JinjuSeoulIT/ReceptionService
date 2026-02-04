package kr.co.seoulit.reception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "?? ?? ??")
@Data
public class EmergencyReceptionDTO implements Serializable {

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

    @Schema(description = "??? ??")
    private Integer triageLevel;

    @Schema(description = "???")
    private String chiefComplaint;

    @Schema(description = "??")
    private Double vitalTemp;

    @Schema(description = "??? ??")
    private Integer vitalBpSystolic;

    @Schema(description = "??? ??")
    private Integer vitalBpDiastolic;

    @Schema(description = "???")
    private Integer vitalHr;

    @Schema(description = "???")
    private Integer vitalRr;

    @Schema(description = "?????")
    private Integer vitalSpo2;

    @Schema(description = "?? ??")
    private String arrivalMode;

    @Schema(description = "??? ??")
    private String triageNote;

    @Schema(description = "?? ??")
    private LocalDateTime createdAt;

    @Schema(description = "?? ??")
    private LocalDateTime updatedAt;
}
