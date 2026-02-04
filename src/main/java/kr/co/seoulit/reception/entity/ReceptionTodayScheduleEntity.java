package kr.co.seoulit.reception.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "RECEPTION_TODAY_SCHEDULE")
@Getter
@Setter
public class ReceptionTodayScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RECEPTION_TODAY_SCHEDULE_SEQ")
    @SequenceGenerator(
            name = "RECEPTION_TODAY_SCHEDULE_SEQ",
            sequenceName = "RECEPTION_TODAY_SCHEDULE_SEQ",
            allocationSize = 1
    )
    @Column(name = "SCHEDULE_ID")
    private Long scheduleId;

    @Column(name = "SCHEDULE_DATE", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "TIME_LABEL", length = 10, nullable = false)
    private String timeLabel;

    @Column(name = "TITLE", length = 200, nullable = false)
    private String title;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;
}
