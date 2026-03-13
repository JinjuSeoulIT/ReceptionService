<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/dto/ReceptionStatusUpdateRequest.java
package kr.co.seoulit.reception.dto;
========
package kr.co.seoulit.reception.outpatient.dto;
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/outpatient/dto/OutpatientReceptionStatusUpdateRequest.java

import lombok.Data;

@Data
public class ReceptionStatusUpdateRequest {
    private String status;
    private Long changedBy;
    private String reasonCode;
    private String reasonText;
}
<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/dto/ReceptionStatusUpdateRequest.java
========




>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/outpatient/dto/OutpatientReceptionStatusUpdateRequest.java
