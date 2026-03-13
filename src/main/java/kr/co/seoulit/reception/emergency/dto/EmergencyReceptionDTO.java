<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/dto/EmergencyReceptionDTO.java
package kr.co.seoulit.reception.dto;
=======
package kr.co.seoulit.reception.emergency.dto;
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/emergency/dto/EmergencyReceptionDTO.java

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "응급 접수 정보")
@Data
public class EmergencyReceptionDTO implements Serializable {

    @Schema(description = "접수 ID")
    private Long receptionId;

    @Schema(description = "접수 번호")
    private String receptionNo;

    @Schema(description = "환자 ID")
    private Long patientId;

    @Schema(description = "환자 이름")
    private String patientName;

<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/dto/EmergencyReceptionDTO.java
    @Schema(description = "내원 유형")
=======
    @Schema(description = "방문 유형")
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/emergency/dto/EmergencyReceptionDTO.java
    private String visitType;

    @Schema(description = "진료과 ID")
    private Long departmentId;

    @Schema(description = "진료과 이름")
    private String departmentName;

<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/dto/EmergencyReceptionDTO.java
    @Schema(description = "의사 ID")
    private Long doctorId;

    @Schema(description = "의사 이름")
=======
    @Schema(description = "의사 식별자")
    private Long doctorId;

    @Schema(description = "의사명")
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/emergency/dto/EmergencyReceptionDTO.java
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

    @Schema(description = "중증도 분류 단계")
    private Integer triageLevel;

    @Schema(description = "주호소")
    private String chiefComplaint;

    @Schema(description = "체온")
    private Double vitalTemp;

    @Schema(description = "수축기 혈압")
    private Integer vitalBpSystolic;

    @Schema(description = "이완기 혈압")
    private Integer vitalBpDiastolic;

    @Schema(description = "심박수")
    private Integer vitalHr;

    @Schema(description = "호흡수")
    private Integer vitalRr;

    @Schema(description = "산소포화도")
    private Integer vitalSpo2;

<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/dto/EmergencyReceptionDTO.java
    @Schema(description = "내원 방법")
    private String arrivalMode;

    @Schema(description = "중증도 메모")
    private String triageNote;

    @Schema(description = "생성 시간")
=======
    @Schema(description = "내원 경로")
    private String arrivalMode;

    @Schema(description = "분류 비고")
    private String triageNote;

    @Schema(description = "등록 일시")
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/emergency/dto/EmergencyReceptionDTO.java
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간")
    private LocalDateTime updatedAt;
}