package kr.co.seoulit.reception.outpatient.dto;

import lombok.Data;

@Data
public class ReceptionStatusUpdateRequest {
    private String status;
    private Long changedBy;
    private String reasonCode;
    private String reasonText;
}




