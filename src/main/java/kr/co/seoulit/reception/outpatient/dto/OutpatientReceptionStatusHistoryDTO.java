<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/dto/ReceptionStatusHistoryDTO.java
package kr.co.seoulit.reception.dto;
========
package kr.co.seoulit.reception.outpatient.dto;
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/outpatient/dto/OutpatientReceptionStatusHistoryDTO.java

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/dto/ReceptionStatusHistoryDTO.java
@Schema(description = "접수 상태 변경 이력")
========
@Schema(description = "외래 상태 이력 정보")
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/outpatient/dto/OutpatientReceptionStatusHistoryDTO.java
@Data
public class ReceptionStatusHistoryDTO {

    @Schema(description = "상태 이력 ID")
    private Long statusHistoryId;

    @Schema(description = "접수 ID")
    private Long receptionId;

    @Schema(description = "변경 전 상태")
    private String fromStatus;

    @Schema(description = "변경 후 상태")
    private String toStatus;

    @Schema(description = "변경자 식별자")
    private Long changedBy;

    @Schema(description = "변경 일시")
    private LocalDateTime changedAt;

    @Schema(description = "사유 코드")
    private String reasonCode;

    @Schema(description = "사유")
    private String reasonText;
<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/dto/ReceptionStatusHistoryDTO.java
}
========
}
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/outpatient/dto/OutpatientReceptionStatusHistoryDTO.java
