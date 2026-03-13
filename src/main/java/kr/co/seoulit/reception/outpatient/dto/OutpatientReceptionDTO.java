<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/dto/ReceptionDTO.java
package kr.co.seoulit.reception.dto;
========
package kr.co.seoulit.reception.outpatient.dto;
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/outpatient/dto/OutpatientReceptionDTO.java

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/dto/ReceptionDTO.java
@Schema(description = "접수 정보")
========
@Schema(description = "외래 접수 정보")
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/outpatient/dto/OutpatientReceptionDTO.java
@Data
public class ReceptionDTO implements Serializable {

    @Schema(description = "접수 ID")
    private Long receptionId;

    @Schema(description = "접수 번호")
    private String receptionNo;

    @Schema(description = "환자 ID")
    private Long patientId;

    @Schema(description = "환자 이름")
    private String patientName;

<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/dto/ReceptionDTO.java
    @Schema(description = "내원 유형")
========
    @Schema(description = "방문 유형")
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/outpatient/dto/OutpatientReceptionDTO.java
    private String visitType;

    @Schema(description = "진료과 ID")
    private Long departmentId;

    @Schema(description = "진료과 이름")
    private String departmentName;

<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/dto/ReceptionDTO.java
    @Schema(description = "의사 ID")
    private Long doctorId;

    @Schema(description = "의사 이름")
========
    @Schema(description = "의사 식별자")
    private Long doctorId;

    @Schema(description = "의사명")
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/outpatient/dto/OutpatientReceptionDTO.java
    private String doctorName;

    @Schema(description = "예약 ID")
    private Long reservationId;

    @Schema(description = "예약 시간")
    private LocalDateTime scheduledAt;

    @Schema(description = "도착 시간")
    private LocalDateTime arrivedAt;

    @Schema(description = "상태")
    private String status;

    @Schema(description = "비고")
    private String note;

    @Schema(description = "사용 여부")
    private Boolean isActive;

    @Schema(description = "비활성 시간")
    private LocalDateTime inactiveAt;

    @Schema(description = "비활성 사유 코드")
    private String inactiveReasonCode;

    @Schema(description = "비활성 사유")
    private String inactiveReasonText;

    @Schema(description = "취소 사유 코드")
    private String cancelReasonCode;

    @Schema(description = "취소 사유")
    private String cancelReasonText;

    @Schema(description = "보류 사유 코드")
    private String holdReasonCode;

    @Schema(description = "보류 사유")
    private String holdReasonText;

    @Schema(description = "등록자")
    private Long createdBy;

    @Schema(description = "수정자")
    private Long updatedBy;

<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/dto/ReceptionDTO.java
    @Schema(description = "생성 시각")
========
    @Schema(description = "등록 일시")
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/outpatient/dto/OutpatientReceptionDTO.java
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;
<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/dto/ReceptionDTO.java
}
========
}
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/outpatient/dto/OutpatientReceptionDTO.java
