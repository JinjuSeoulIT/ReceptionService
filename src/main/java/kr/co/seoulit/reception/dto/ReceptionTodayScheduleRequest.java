package kr.co.seoulit.reception.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReceptionTodayScheduleRequest {
    private LocalDate scheduleDate;
    private String timeLabel;
    private String title;
    private Integer sortOrder;
}
