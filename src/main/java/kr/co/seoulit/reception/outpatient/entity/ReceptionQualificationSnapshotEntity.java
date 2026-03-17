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

import java.time.LocalDateTime;

@Entity
@Table(name = "reception_qualification_snap")
@Getter
@Setter
@NoArgsConstructor
public class ReceptionQualificationSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RECEPTION_QUAL_SNAP")
    @SequenceGenerator(name = "SEQ_RECEPTION_QUAL_SNAP", sequenceName = "SEQ_RECEPTION_QUAL_SNAP", allocationSize = 1)
    @Column(name = "qualification_snapshot_id")
    private Long qualificationSnapshotId;

    @Column(name = "reception_id", nullable = false)
    private Long receptionId;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "snapshot_datetime", nullable = false)
    private LocalDateTime snapshotDatetime;

    @Column(name = "result_cd", length = 30)
    private String resultCd;

    @Column(name = "payer_type_cd", length = 30)
    private String payerTypeCd;

    @Column(name = "insurance_type_cd", length = 30)
    private String insuranceTypeCd;

    @Column(name = "valid_yn", length = 1, nullable = false)
    private String validYn = "Y";

    @Column(name = "source_system_cd", length = 30)
    private String sourceSystemCd;
}
