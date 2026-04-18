package kr.co.seoulit.reception.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "예약 접수 정보")
@Data
public class ReservationReceptionDTO implements Serializable {

    @Schema(description = "예약 ID")
    private Long reservationId;

    @Schema(description = "예약 번호")
    private String reservationNo;

    @Schema(description = "환자 ID")
    private Long patientId;

    @Schema(description = "환자 이름")
    private String patientName;

    @Schema(description = "진료과 ID")
    private String departmentId;

    @Schema(description = "진료과 이름")
    private String departmentName;

    @Schema(description = "의사 ID")
    private String doctorId;

    @Schema(description = "의사 이름")
    private String doctorName;

    @Schema(description = "예약 시간")
    private LocalDateTime reservedAt;

    @Schema(description = "상태")
    private String status;

    @Schema(description = "메모")
    private String note;

    @Schema(description = "사용 여부")
    private Boolean isActive;

    @Schema(description = "비활성 시간")
    private LocalDateTime inactiveAt;

    @Schema(description = "비활성 사유 코드")
    private String inactiveReasonCode;

    @Schema(description = "비활성 사유")
    private String inactiveReasonText;

    @Schema(description = "취소 시간")
    private LocalDateTime canceledAt;

    @Schema(description = "취소 사유 코드")
    private String cancelReasonCode;

    @Schema(description = "취소 사유")
    private String cancelReasonText;

    @Schema(description = "등록자")
    private Long createdBy;

    @Schema(description = "수정자")
    private Long updatedBy;

    @Schema(description = "등록 일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시")
    private LocalDateTime updatedAt;
}
