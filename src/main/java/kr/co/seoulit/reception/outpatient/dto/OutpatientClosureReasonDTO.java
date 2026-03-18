package kr.co.seoulit.reception.outpatient.dto;

import lombok.Data;

@Data
public class OutpatientClosureReasonDTO {
    private String closureReasonCd;
    private String closureReasonName;
    private String reasonGroupCd;
    private String usableYn;
    private Integer sortOrder;
}

