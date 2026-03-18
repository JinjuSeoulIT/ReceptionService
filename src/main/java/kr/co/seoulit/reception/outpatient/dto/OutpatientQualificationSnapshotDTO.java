package kr.co.seoulit.reception.outpatient.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutpatientQualificationSnapshotDTO {
    private Long qualificationSnapshotId;
    private Long receptionId;
    private Long patientId;
    private LocalDateTime snapshotDatetime;
    private String resultCd;
    private String payerTypeCd;
    private String insuranceTypeCd;
    private String validYn;
    private String sourceSystemCd;
}

