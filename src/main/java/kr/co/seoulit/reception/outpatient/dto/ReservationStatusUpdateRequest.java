<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/dto/ReservationStatusUpdateRequest.java
package kr.co.seoulit.reception.dto;
========
package kr.co.seoulit.reception.reservation.dto;
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/reservation/dto/ReservationReceptionStatusUpdateRequest.java

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "예약 상태 변경 요청")
@Data
public class ReservationStatusUpdateRequest {

    @Schema(description = "상태")
    private String status;

    @Schema(description = "변경자 식별자")
    private Long changedBy;

    @Schema(description = "사유 코드")
    private String reasonCode;

    @Schema(description = "사유")
    private String reasonText;
}