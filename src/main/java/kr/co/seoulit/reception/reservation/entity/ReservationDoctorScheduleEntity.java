package kr.co.seoulit.reception.reservation.entity;

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

import java.time.LocalDate;

@Entity
@Table(name = "reservation_doctor_schedule")
@Getter
@Setter
@NoArgsConstructor
public class ReservationDoctorScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RESERVATION_DOCTOR_SCH")
    @SequenceGenerator(
            name = "SEQ_RESERVATION_DOCTOR_SCH",
            sequenceName = "SEQ_RESERVATION_DOCTOR_SCH",
            allocationSize = 1
    )
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "doctor_id", nullable = false)
    private String doctorId;

    @Column(name = "dept_id")
    private String deptId;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "start_time", length = 5, nullable = false)
    private String startTime;

    @Column(name = "end_time", length = 5, nullable = false)
    private String endTime;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "active_yn", length = 1, nullable = false)
    private String activeYn = "Y";
}
