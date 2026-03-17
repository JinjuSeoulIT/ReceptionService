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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reception_outpatient_detail")
@Getter
@Setter
@NoArgsConstructor
public class OutpatientReceptionDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RECEPTION_OUTPATIENT_DTL")
    @SequenceGenerator(
            name = "SEQ_RECEPTION_OUTPATIENT_DTL",
            sequenceName = "SEQ_RECEPTION_OUTPATIENT_DTL",
            allocationSize = 1
    )
    @Column(name = "outpatient_detail_id")
    private Long outpatientDetailId;

    @Column(name = "reception_id", nullable = false)
    private Long receptionId;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "visit_purpose_cd", length = 30)
    private String visitPurposeCd;

    @Column(name = "primary_symptom", length = 500)
    private String primarySymptom;

    @Column(name = "consultation_type_cd", length = 30)
    private String consultationTypeCd;

    @Column(name = "insurance_apply_yn", length = 1, nullable = false)
    private String insuranceApplyYn = "N";

    @Column(name = "active_yn", length = 1, nullable = false)
    private String activeYn = "Y";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
