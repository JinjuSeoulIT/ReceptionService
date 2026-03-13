package kr.co.seoulit.reception.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reception_inpatient")
@Getter
@Setter
@NoArgsConstructor
public class ReceptionInpatientEntity {

    @Id
    @Column(name = "reception_id")
    private Long receptionId;

    @Column(name = "admission_plan_at", nullable = false)
    private LocalDateTime admissionPlanAt;

    @Column(name = "ward_id")
    private Long wardId;

    @Column(name = "room_id")
    private Long roomId;
}
