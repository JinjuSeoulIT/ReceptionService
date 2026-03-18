package kr.co.seoulit.reception.outpatient.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutpatientVisitClosureHistoryDTO {
    private Long visitClosureHistoryId;
    private Long visitClosureId;
    private String beforeStatusCd;
    private String afterStatusCd;
    private Long changedBy;
    private LocalDateTime changedAt;
    private String changeReason;
}

