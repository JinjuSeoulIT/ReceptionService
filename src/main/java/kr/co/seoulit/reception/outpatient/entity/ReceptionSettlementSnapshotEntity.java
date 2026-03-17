package kr.co.seoulit.reception.outpatient.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reception_settlement_snapshot")
@Getter
@Setter
@NoArgsConstructor
public class ReceptionSettlementSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RECEPTION_SETTLEMENT_SNAP")
    @SequenceGenerator(
            name = "SEQ_RECEPTION_SETTLEMENT_SNAP",
            sequenceName = "SEQ_RECEPTION_SETTLEMENT_SNAP",
            allocationSize = 1
    )
    @Column(name = "settlement_snapshot_id")
    private Long settlementSnapshotId;

    @Column(name = "reception_id", nullable = false)
    private Long receptionId;

    @Column(name = "pay_status_cd", length = 30)
    private String payStatusCd;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "insurance_amount", precision = 15, scale = 2)
    private BigDecimal insuranceAmount;

    @Column(name = "patient_amount", precision = 15, scale = 2)
    private BigDecimal patientAmount;

    @Column(name = "snapshot_datetime", nullable = false)
    private LocalDateTime snapshotDatetime;
}
