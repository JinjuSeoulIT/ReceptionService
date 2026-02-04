package kr.co.seoulit.reception.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reception_emergency")
@Getter
@Setter
@NoArgsConstructor
public class ReceptionEmergencyEntity {

    @Id
    @Column(name = "reception_id")
    private Long receptionId;

    @Column(name = "triage_level", nullable = false)
    private Integer triageLevel;

    @Column(name = "chief_complaint", length = 255, nullable = false)
    private String chiefComplaint;

    @Column(name = "vital_temp")
    private Double vitalTemp;

    @Column(name = "vital_bp_systolic")
    private Integer vitalBpSystolic;

    @Column(name = "vital_bp_diastolic")
    private Integer vitalBpDiastolic;

    @Column(name = "vital_hr")
    private Integer vitalHr;

    @Column(name = "vital_rr")
    private Integer vitalRr;

    @Column(name = "vital_spo2")
    private Integer vitalSpo2;

    @Column(name = "arrival_mode", length = 20)
    private String arrivalMode;

    @Column(name = "triage_note", length = 255)
    private String triageNote;
}
