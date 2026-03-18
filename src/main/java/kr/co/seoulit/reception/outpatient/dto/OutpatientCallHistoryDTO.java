package kr.co.seoulit.reception.outpatient.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutpatientCallHistoryDTO {
    private Long callHistoryId;
    private Long waitingQueueId;
    private LocalDateTime callDatetime;
    private Long callUserId;
    private Integer callCount;
    private String callResultCd;
    private String remark;
}

