package kr.co.seoulit.reception.inpatient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "입원 접수 정보")
@Data
public class InpatientReceptionDTO implements Serializable {

    @Schema(description = "접수 ID")
    private Long receptionId;

    @Schema(description = "접수 번호")
    private String receptionNo;

    @Schema(description = "환자 ID")
    private Long patientId;

    @Schema(description = "환자 이름")
    private String patientName;

    @Schema(description = "방문 유형")
    private String visitType;

    @Schema(description = "진료과 ID")
    private String departmentId;

    @Schema(description = "진료과 이름")
    private String departmentName;

    @Schema(description = "의사 ID")
    private String doctorId;

    @Schema(description = "의사 이름")
    private String doctorName;

    @Schema(description = "예약 ID")
    private Long reservationId;

    @Schema(description = "예약 시간")
    private LocalDateTime scheduledAt;

    @Schema(description = "도착 시간")
    private LocalDateTime arrivedAt;

    @Schema(description = "상태")
    private String status;

    @Schema(description = "메모")
    private String note;

    @Schema(description = "사용 여부")
    private Boolean isActive;

    @Schema(description = "입원 예정 일시")
    private LocalDateTime admissionPlanAt;

    @Schema(description = "병동 ID")
    private Long wardId;

    @Schema(description = "병실 ID")
    private Long roomId;

    @Schema(description = "등록 일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시")
    private LocalDateTime updatedAt;
}
