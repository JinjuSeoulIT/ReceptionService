package kr.co.seoulit.reception.emergency.entity;

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
@Table(name = "emergency_triage")
@Getter
@Setter
@NoArgsConstructor
public class EmergencyTriageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EMERGENCY_TRIAGE")
    @SequenceGenerator(name = "SEQ_EMERGENCY_TRIAGE", sequenceName = "SEQ_EMERGENCY_TRIAGE", allocationSize = 1)
    @Column(name = "emergency_triage_id")
    private Long emergencyTriageId;

    @Column(name = "reception_id", nullable = false)
    private Long receptionId;

    @Column(name = "triage_level_cd", length = 20, nullable = false)
    private String triageLevelCd;

    @Column(name = "triage_datetime", nullable = false)
    private LocalDateTime triageDatetime;

    @Column(name = "triage_user_id")
    private Long triageUserId;

    @Column(name = "triage_note", length = 1000)
    private String triageNote;

    @Column(name = "priority_score")
    private Double priorityScore;

    @Column(name = "active_yn", length = 1, nullable = false)
    private String activeYn = "Y";
}

