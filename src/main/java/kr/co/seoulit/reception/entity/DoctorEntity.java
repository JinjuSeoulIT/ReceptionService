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
@Table(name = "doctor")
@Getter
@Setter
@NoArgsConstructor
public class DoctorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DOCTOR")
    @SequenceGenerator(name = "SEQ_DOCTOR", sequenceName = "SEQ_DOCTOR", allocationSize = 1)
    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "doctor_name", length = 50, nullable = false)
    private String doctorName;

    @Column(name = "department_id")
    private Long departmentId;
}
