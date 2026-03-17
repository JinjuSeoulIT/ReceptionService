package kr.co.seoulit.reception.emergency.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_detail")
@Getter
@Setter
@NoArgsConstructor
public class EmergencyReceptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EMERGENCY_DETAIL")
    @SequenceGenerator(name = "SEQ_EMERGENCY_DETAIL", sequenceName = "SEQ_EMERGENCY_DETAIL", allocationSize = 1)
    @Column(name = "emergency_detail_id")
    private Long emergencyDetailId;

    @Column(name = "reception_id")
    private Long receptionId;

    @Column(name = "emergency_note", length = 1000)
    private String chiefComplaint;

    @Column(name = "temperature")
    private Double vitalTemp;

    @Column(name = "blood_pressure", length = 30)
    private String bloodPressure;

    @Column(name = "pulse_rate")
    private Integer vitalHr;

    @Column(name = "arrival_transport_cd", length = 30)
    private String arrivalMode;

    @Column(name = "arrival_datetime")
    private LocalDateTime arrivalDatetime;

    @Column(name = "active_yn", length = 1, nullable = false)
    private String activeYn = "Y";

    @Transient
    private Integer vitalBpSystolic;

    @Transient
    private Integer vitalBpDiastolic;

    @Transient
    private Integer vitalRr;

    @Transient
    private Integer vitalSpo2;

    @Transient
    private Integer triageLevel;

    @Transient
    private String triageNote;
}



