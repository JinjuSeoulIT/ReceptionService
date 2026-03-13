package kr.co.seoulit.reception.reservation.service;

import kr.co.seoulit.reception.common.audit.AuditLogService;
import kr.co.seoulit.reception.mapstruct.ReservationReqMapStruct;
import kr.co.seoulit.reception.mapstruct.ReservationResMapStruct;
import kr.co.seoulit.reception.reservation.dto.ReservationReceptionDTO;
import kr.co.seoulit.reception.reservation.entity.ReservationBookingRuleEntity;
import kr.co.seoulit.reception.reservation.entity.ReservationDoctorScheduleEntity;
import kr.co.seoulit.reception.reservation.entity.ReservationReceptionEntity;
import kr.co.seoulit.reception.reservation.entity.ReservationStatusHistoryEntity;
import kr.co.seoulit.reception.reservation.entity.ReservationTimeSlotEntity;
import kr.co.seoulit.reception.reservation.mapper.ReservationReceptionMapper;
import kr.co.seoulit.reception.reservation.repository.ReservationBookingRuleRepository;
import kr.co.seoulit.reception.reservation.repository.ReservationDoctorScheduleRepository;
import kr.co.seoulit.reception.reservation.repository.ReservationReceptionRepository;
import kr.co.seoulit.reception.reservation.repository.ReservationStatusHistoryRepository;
import kr.co.seoulit.reception.reservation.repository.ReservationTimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationReceptionServiceImpl implements ReservationReceptionService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ReservationReceptionRepository reservationRepository;
    private final ReservationReceptionMapper reservationMyBatisMapper;
    private final ReservationResMapStruct reservationResMapStruct;
    private final ReservationReqMapStruct reservationReqMapStruct;
    private final ReservationStatusHistoryRepository reservationStatusHistoryRepository;
    private final ReservationDoctorScheduleRepository reservationDoctorScheduleRepository;
    private final ReservationTimeSlotRepository reservationTimeSlotRepository;
    private final ReservationBookingRuleRepository reservationBookingRuleRepository;
    private final AuditLogService auditLogService;

    @Override
    public List<ReservationReceptionDTO> getReservationList(Map<String, Object> searchCondition) {
        String searchType = (String) searchCondition.get("searchType");
        String searchValue = (String) searchCondition.get("searchValue");
        return reservationMyBatisMapper.selectReservations(searchType, searchValue);
    }

    @Override
    @Cacheable(key = "#reservationId", value = "RESERVATION")
    public ReservationReceptionDTO getReservation(Long reservationId) {
        ReservationReceptionEntity entity = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found. reservationId=" + reservationId));
        return reservationResMapStruct.toDto(entity);
    }

    @Override
    @Transactional
    public void createReservation(ReservationReceptionDTO reservation) {
        if (reservation.getReservationNo() == null || reservation.getReservationNo().isBlank()) {
            throw new IllegalArgumentException("reservationNo is required");
        }
        if (reservationRepository.existsByReservationNo(reservation.getReservationNo())) {
            throw new IllegalArgumentException("Duplicated reservationNo: " + reservation.getReservationNo());
        }
        if (reservation.getReservedAt() == null) {
            throw new IllegalArgumentException("reservedAt is required");
        }

        ReservationReceptionEntity entity = reservationReqMapStruct.toEntity(reservation);
        entity.setPatientId(resolveOrCreatePatientId(entity.getPatientId(), entity.getPatientName()));
        if (entity.getPatientName() == null || entity.getPatientName().isBlank()) {
            entity.setPatientName(resolvePatientName(entity.getPatientId()));
        }
        if (entity.getDepartmentName() == null || entity.getDepartmentName().isBlank()) {
            entity.setDepartmentName(resolveDepartmentName(entity.getDepartmentId()));
        }
        if (entity.getDoctorId() != null && (entity.getDoctorName() == null || entity.getDoctorName().isBlank())) {
            entity.setDoctorName(resolveDoctorName(entity.getDoctorId()));
        }
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("RESERVED");
        }
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }

        ReservationReceptionEntity saved = reservationRepository.save(entity);
        ensureBookingRule(saved);
        ReservationDoctorScheduleEntity schedule = ensureDoctorSchedule(saved);
        ensureTimeSlot(saved, schedule);
        saveReservationStatusHistory(saved.getReservationId(), null, saved.getStatus(), reservation.getCreatedBy(), "Reservation created");

        auditLogService.log(
                "RESERVATION",
                saved.getReservationId(),
                "CREATE",
                reservation.getCreatedBy(),
                null,
                null,
                null,
                reservationResMapStruct.toDto(saved)
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "RESERVATION", key = "#reservationId")
    public void updateReservation(Long reservationId, ReservationReceptionDTO reservation) {
        ReservationReceptionEntity existing = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found. reservationId=" + reservationId));

        ReservationReceptionDTO before = reservationResMapStruct.toDto(existing);
        String beforeStatus = existing.getStatus();

        if (reservation.getReservationNo() != null && !reservation.getReservationNo().isBlank()) {
            existing.setReservationNo(reservation.getReservationNo());
        }
        if (reservation.getPatientId() != null) {
            existing.setPatientId(reservation.getPatientId());
        }
        if (reservation.getPatientName() != null) {
            existing.setPatientName(reservation.getPatientName());
        }
        if (reservation.getPatientId() == null && reservation.getPatientName() != null) {
            existing.setPatientId(resolveOrCreatePatientId(null, reservation.getPatientName()));
        }
        if (reservation.getPatientId() != null
                && (reservation.getPatientName() == null || reservation.getPatientName().isBlank())) {
            existing.setPatientName(resolvePatientName(reservation.getPatientId()));
        }
        if (reservation.getDepartmentId() != null) {
            existing.setDepartmentId(reservation.getDepartmentId());
        }
        if (reservation.getDepartmentName() != null) {
            existing.setDepartmentName(reservation.getDepartmentName());
        } else if (reservation.getDepartmentId() != null) {
            existing.setDepartmentName(resolveDepartmentName(reservation.getDepartmentId()));
        }
        if (reservation.getDoctorId() != null) {
            existing.setDoctorId(reservation.getDoctorId());
        }
        if (reservation.getDoctorName() != null) {
            existing.setDoctorName(reservation.getDoctorName());
        } else if (reservation.getDoctorId() != null) {
            existing.setDoctorName(resolveDoctorName(reservation.getDoctorId()));
        }
        if (reservation.getReservedAt() != null) {
            existing.setReservedAt(reservation.getReservedAt());
        }
        if (reservation.getStatus() != null && !reservation.getStatus().isBlank()) {
            existing.setStatus(reservation.getStatus());
        }
        if (reservation.getNote() != null) {
            existing.setNote(reservation.getNote());
        }
        if (reservation.getIsActive() != null) {
            existing.setIsActive(reservation.getIsActive());
        }
        if (reservation.getInactiveAt() != null) {
            existing.setInactiveAt(reservation.getInactiveAt());
        }
        if (reservation.getInactiveReasonCode() != null) {
            existing.setInactiveReasonCode(reservation.getInactiveReasonCode());
        }
        if (reservation.getInactiveReasonText() != null) {
            existing.setInactiveReasonText(reservation.getInactiveReasonText());
        }
        if (reservation.getCanceledAt() != null) {
            existing.setCanceledAt(reservation.getCanceledAt());
        }
        if (reservation.getCancelReasonCode() != null) {
            existing.setCancelReasonCode(reservation.getCancelReasonCode());
        }
        if (reservation.getCancelReasonText() != null) {
            existing.setCancelReasonText(reservation.getCancelReasonText());
        }
        if (reservation.getCreatedBy() != null) {
            existing.setCreatedBy(reservation.getCreatedBy());
        }
        if (reservation.getUpdatedBy() != null) {
            existing.setUpdatedBy(reservation.getUpdatedBy());
        }

        ReservationReceptionEntity saved = reservationRepository.save(existing);
        ensureBookingRule(saved);
        ReservationDoctorScheduleEntity schedule = ensureDoctorSchedule(saved);
        ensureTimeSlot(saved, schedule);
        if (!equalsIgnoreCase(beforeStatus, saved.getStatus())) {
            saveReservationStatusHistory(
                    saved.getReservationId(),
                    beforeStatus,
                    saved.getStatus(),
                    reservation.getUpdatedBy(),
                    "Reservation updated"
            );
        }

        auditLogService.log(
                "RESERVATION",
                saved.getReservationId(),
                "UPDATE",
                reservation.getUpdatedBy(),
                null,
                null,
                before,
                reservationResMapStruct.toDto(saved)
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "RESERVATION", key = "#reservationId")
    public ReservationReceptionDTO updateReservationStatus(Long reservationId, String status, Long changedBy, String reasonCode, String reasonText) {
        return doUpdateReservationStatus(reservationId, status, changedBy, reasonCode, reasonText);
    }

    private ReservationReceptionDTO doUpdateReservationStatus(
            Long reservationId,
            String status,
            Long changedBy,
            String reasonCode,
            String reasonText
    ) {
        ReservationReceptionEntity existing = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found. reservationId=" + reservationId));

        ReservationReceptionDTO before = reservationResMapStruct.toDto(existing);
        String beforeStatus = existing.getStatus();

        existing.setStatus(status);
        existing.setUpdatedBy(changedBy);
        if ("CANCELED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            existing.setIsActive(false);
            existing.setCanceledAt(LocalDateTime.now());
            existing.setCancelReasonCode(reasonCode);
            existing.setCancelReasonText(reasonText);
            existing.setInactiveAt(LocalDateTime.now());
            existing.setInactiveReasonCode(reasonCode);
            existing.setInactiveReasonText(reasonText);
        }
        if ("INACTIVE".equalsIgnoreCase(status)) {
            existing.setIsActive(false);
            existing.setInactiveAt(LocalDateTime.now());
            existing.setInactiveReasonCode(reasonCode);
            existing.setInactiveReasonText(reasonText);
        }

        ReservationReceptionEntity saved = reservationRepository.save(existing);
        ensureBookingRule(saved);
        ReservationDoctorScheduleEntity schedule = ensureDoctorSchedule(saved);
        ensureTimeSlot(saved, schedule);
        saveReservationStatusHistory(saved.getReservationId(), beforeStatus, status, changedBy, reasonText);

        auditLogService.log(
                "RESERVATION",
                saved.getReservationId(),
                "STATUS_CHANGE",
                changedBy,
                reasonCode,
                reasonText,
                before,
                reservationResMapStruct.toDto(saved)
        );

        return reservationResMapStruct.toDto(saved);
    }

    private void saveReservationStatusHistory(
            Long reservationId,
            String beforeStatus,
            String afterStatus,
            Long changedBy,
            String changeReason
    ) {
        ReservationStatusHistoryEntity history = new ReservationStatusHistoryEntity();
        history.setReservationId(reservationId);
        history.setBeforeStatusCd(beforeStatus);
        history.setAfterStatusCd(afterStatus == null ? "UNKNOWN" : afterStatus);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(changedBy);
        history.setChangeReason(changeReason);
        reservationStatusHistoryRepository.save(history);
    }

    private ReservationDoctorScheduleEntity ensureDoctorSchedule(ReservationReceptionEntity reservation) {
        if (reservation.getDoctorId() == null || reservation.getReservedAt() == null) {
            return null;
        }

        LocalDate scheduleDate = reservation.getReservedAt().toLocalDate();
        return reservationDoctorScheduleRepository
                .findTopByDoctorIdAndDeptIdAndScheduleDateOrderByScheduleIdDesc(
                        reservation.getDoctorId(),
                        reservation.getDepartmentId(),
                        scheduleDate
                )
                .orElseGet(() -> {
                    ReservationDoctorScheduleEntity schedule = new ReservationDoctorScheduleEntity();
                    schedule.setDoctorId(reservation.getDoctorId());
                    schedule.setDeptId(reservation.getDepartmentId());
                    schedule.setScheduleDate(scheduleDate);
                    schedule.setStartTime("09:00");
                    schedule.setEndTime("18:00");
                    schedule.setMaxCapacity(100);
                    schedule.setActiveYn("Y");
                    return reservationDoctorScheduleRepository.save(schedule);
                });
    }

    private void ensureTimeSlot(ReservationReceptionEntity reservation, ReservationDoctorScheduleEntity schedule) {
        if (schedule == null || reservation.getReservedAt() == null) {
            return;
        }

        LocalDateTime start = reservation.getReservedAt();
        LocalDateTime end = start.plusMinutes(30);
        ReservationTimeSlotEntity timeSlot = new ReservationTimeSlotEntity();
        timeSlot.setScheduleId(schedule.getScheduleId());
        timeSlot.setSlotStartDatetime(start);
        timeSlot.setSlotEndDatetime(end);
        timeSlot.setSlotStatusCd(mapSlotStatus(reservation.getStatus()));
        timeSlot.setReservationId(reservation.getReservationId());
        reservationTimeSlotRepository.save(timeSlot);
    }

    private void ensureBookingRule(ReservationReceptionEntity reservation) {
        if (reservation.getDepartmentId() == null || reservation.getDoctorId() == null) {
            return;
        }

        reservationBookingRuleRepository
                .findTopByDeptIdAndDoctorIdOrderByBookingRuleIdDesc(reservation.getDepartmentId(), reservation.getDoctorId())
                .orElseGet(() -> {
                    ReservationBookingRuleEntity rule = new ReservationBookingRuleEntity();
                    rule.setDeptId(reservation.getDepartmentId());
                    rule.setDoctorId(reservation.getDoctorId());
                    rule.setMinLeadMin(10);
                    rule.setMaxLeadDay(30);
                    rule.setOverbookAllowYn("N");
                    rule.setCancelDeadlineMin(60);
                    rule.setPriorityExpr("DEFAULT");
                    rule.setActiveYn("Y");
                    return reservationBookingRuleRepository.save(rule);
                });
    }

    private String mapSlotStatus(String reservationStatus) {
        if (reservationStatus == null || reservationStatus.isBlank()) {
            return "RESERVED";
        }
        String normalized = reservationStatus.trim().toUpperCase();
        if ("CANCELED".equals(normalized) || "CANCELLED".equals(normalized)) {
            return "CANCELED";
        }
        if ("COMPLETED".equals(normalized) || "DONE".equals(normalized)) {
            return "COMPLETED";
        }
        return "RESERVED";
    }

    private boolean equalsIgnoreCase(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equalsIgnoreCase(b);
    }

    private Long resolveOrCreatePatientId(Long patientId, String patientName) {
        if (patientId != null) {
            return patientId;
        }
        throw new IllegalArgumentException("patientId is required");
    }

    private String resolvePatientName(Long patientId) {
        if (patientId == null) {
            return null;
        }
        return "PATIENT-" + patientId;
    }

    private String resolveDepartmentName(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return "DEPT-" + departmentId;
    }

    private String resolveDoctorName(Long doctorId) {
        if (doctorId == null) {
            return null;
        }
        return "DOCTOR-" + doctorId;
    }
}
