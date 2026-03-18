package kr.co.seoulit.reception.outpatient.dto;

import lombok.Data;

@Data
public class OutpatientQualificationItemDTO {
    private Long qualificationItemId;
    private Long qualificationSnapshotId;
    private String itemName;
    private String itemValue;
    private String itemStatusCd;
    private Integer displayOrder;
}

