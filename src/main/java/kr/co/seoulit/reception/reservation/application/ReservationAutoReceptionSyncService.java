package kr.co.seoulit.reception.reservation.application;

import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.repository.OutpatientReceptionRepository;
import kr.co.seoulit.reception.outpatient.service.OutpatientReceptionService;
import kr.co.seoulit.reception.reservation.dto.ReservationReceptionDTO;
import kr.co.seoulit.reception.reservation.service.ReservationReceptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationAutoReceptionSyncService {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");
    private static final Long SYSTEM_USER_ID = 0L;
    private static final String WAITING_STATUS = "WAITING";
    private static final String OUTPATIENT_VISIT_TYPE = "OUTPATIENT";
    private static final String AUTO_SYNC_NOTE = "예약 당일 자동 외래접수 생성";

    private final OutpatientReceptionRepository outpatientReceptionRepository;
    private final OutpatientReceptionService outpatientReceptionService;
    private final ReservationReceptionService reservationReceptionService;

    @Transactional
    public void syncTodayReservations() {
        syncReservationsFor(LocalDate.now(ASIA_SEOUL));
    }

    public void syncReservationsFor(LocalDate targetDate) {
        List<ReservationReceptionDTO> reservations = reservationReceptionService.getAutoSyncReservations(targetDate);

        if (reservations.isEmpty()) {
            log.debug("No same-day reservations to sync for {}", targetDate);
            return;
        }

        int createdCount = 0;
        int completedCount = 0;
        int skippedExistingCount = 0;
        int failedCount = 0;

        for (ReservationReceptionDTO reservation : reservations) {
            Long reservationId = reservation.getReservationId();
            try {
                if (outpatientReceptionRepository.existsByReservationId(reservationId)) {
                    reservationReceptionService.completeReservationByAutoSync(reservationId);
                    completedCount++;
                    skippedExistingCount++;
                    continue;
                }

                outpatientReceptionService.createReception(toOutpatientReceptionDto(reservation));
                createdCount++;
                reservationReceptionService.completeReservationByAutoSync(reservationId);
                completedCount++;
            } catch (Exception ex) {
                failedCount++;
                log.error(
                        "Failed to auto-sync reservation {} scheduled at {}",
                        reservationId,
                        reservation.getReservedAt(),
                        ex
                );
            }
        }

        log.info(
                "Reservation auto-sync finished for {}: targets={}, created={}, completed={}, skippedExisting={}, failed={}",
                targetDate,
                reservations.size(),
                createdCount,
                completedCount,
                skippedExistingCount,
                failedCount
        );
    }

    private OutpatientReceptionDTO toOutpatientReceptionDto(ReservationReceptionDTO reservation) {
        OutpatientReceptionDTO dto = new OutpatientReceptionDTO();
        dto.setPatientId(reservation.getPatientId());
        dto.setVisitType(OUTPATIENT_VISIT_TYPE);
        dto.setDepartmentId(reservation.getDepartmentId());
        dto.setDoctorId(reservation.getDoctorId());
        dto.setReservationId(reservation.getReservationId());
        dto.setScheduledAt(reservation.getReservedAt());
        dto.setStatus(WAITING_STATUS);
        dto.setNote(hasText(reservation.getNote()) ? reservation.getNote().trim() : AUTO_SYNC_NOTE);
        dto.setCreatedBy(SYSTEM_USER_ID);
        dto.setUpdatedBy(SYSTEM_USER_ID);
        return dto;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
