package kr.co.seoulit.reception.service;

import kr.co.seoulit.reception.dto.ReceptionChecklistItemDTO;
import kr.co.seoulit.reception.dto.ReceptionDashboardDTO;
import kr.co.seoulit.reception.dto.ReceptionTodayScheduleDTO;
import kr.co.seoulit.reception.entity.ReceptionChecklistItemEntity;
import kr.co.seoulit.reception.entity.ReceptionTodayScheduleEntity;
import kr.co.seoulit.reception.repository.ReceptionChecklistItemRepository;
import kr.co.seoulit.reception.repository.ReceptionTodayScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceptionDashboardServiceImpl implements ReceptionDashboardService {

    private final ReceptionTodayScheduleRepository receptionTodayScheduleRepository;
    private final ReceptionChecklistItemRepository receptionChecklistItemRepository;

    @Override
    public ReceptionDashboardDTO getDashboard() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<ReceptionTodayScheduleDTO> schedules = receptionTodayScheduleRepository
                .findByScheduleDateOrderBySortOrderAsc(today)
                .stream()
                .map(this::toScheduleDto)
                .collect(Collectors.toList());

        List<ReceptionChecklistItemDTO> checklist = receptionChecklistItemRepository
                .findByCheckDateOrderBySortOrderAsc(today)
                .stream()
                .map(this::toChecklistDto)
                .collect(Collectors.toList());

        ReceptionDashboardDTO dto = new ReceptionDashboardDTO();
        dto.setTodaySchedule(schedules);
        dto.setChecklist(checklist);
        return dto;
    }

    private ReceptionTodayScheduleDTO toScheduleDto(ReceptionTodayScheduleEntity entity) {
        ReceptionTodayScheduleDTO dto = new ReceptionTodayScheduleDTO();
        dto.setTime(entity.getTimeLabel());
        dto.setLabel(entity.getTitle());
        return dto;
    }

    private ReceptionChecklistItemDTO toChecklistDto(ReceptionChecklistItemEntity entity) {
        ReceptionChecklistItemDTO dto = new ReceptionChecklistItemDTO();
        dto.setLabel(entity.getLabel());
        dto.setDone(entity.isDone());
        return dto;
    }
}
