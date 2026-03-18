package kr.co.seoulit.reception.outpatient.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutpatientReceptionAuditDTO {
    private Long receptionAuditId;
    private Long receptionId;
    private String changeTypeCd;
    private String changeFieldNm;
    private String beforeValue;
    private String afterValue;
    private String changeReason;
    private Long changedBy;
    private LocalDateTime changedAt;
}

