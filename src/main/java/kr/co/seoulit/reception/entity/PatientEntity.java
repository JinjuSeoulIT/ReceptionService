package kr.co.seoulit.reception.entity;

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

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
public class PatientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PATIENT")
    @SequenceGenerator(name = "SEQ_PATIENT", sequenceName = "SEQ_PATIENT", allocationSize = 1)
    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "patient_name", length = 50, nullable = false)
    private String patientName;
}
