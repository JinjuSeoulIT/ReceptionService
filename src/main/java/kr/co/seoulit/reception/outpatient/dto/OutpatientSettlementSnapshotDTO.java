package kr.co.seoulit.reception.outpatient.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OutpatientSettlementSnapshotDTO {
    private Long settlementSnapshotId;
    private Long receptionId;
    private String payStatusCd;
    private BigDecimal totalAmount;
    private BigDecimal insuranceAmount;
    private BigDecimal patientAmount;
    private LocalDateTime snapshotDatetime;
}

