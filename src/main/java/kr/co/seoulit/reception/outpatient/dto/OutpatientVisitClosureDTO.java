package kr.co.seoulit.reception.outpatient.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutpatientVisitClosureDTO {
    private Long visitClosureId;
    private Long receptionId;
    private String closureStatusCd;
    private LocalDateTime closureDatetime;
    private Long closureUserId;
    private String closureReasonCd;
    private String remark;
    private String activeYn;
}

