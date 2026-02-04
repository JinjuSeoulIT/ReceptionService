package kr.co.seoulit.reception.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReceptionDashboardDTO {
    private List<ReceptionTodayScheduleDTO> todaySchedule;
    private List<ReceptionChecklistItemDTO> checklist;
}
