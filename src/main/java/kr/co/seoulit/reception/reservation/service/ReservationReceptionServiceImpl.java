package kr.co.seoulit.reception.reservation.service;

import kr.co.seoulit.common.audit.AuditLogService;
import kr.co.seoulit.common.sequence.ReceptionNumberSequenceClient;
import kr.co.seoulit.reception.reservation.mapstruct.ReservationReqMapStruct;
import kr.co.seoulit.reception.reservation.mapstruct.ReservationResMapStruct;
import kr.co.seoulit.common.client.PatientServiceClient;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationReceptionServiceImpl implements ReservationReceptionService {

    private static final Long AUTO_SYNC_SYSTEM_USER_ID = 0L;
    private static final String AUTO_SYNC_REASON_CODE = "AUTO_SYNC";
    private static final String AUTO_SYNC_REASON_TEXT = "예약 당일 자동 외래접수 생성";
    private static final String RESERVED_STATUS = "RESERVED";
    private static final String COMPLETED_STATUS = "COMPLETED";
    private static final Set<String> REASON_REQUIRED_STATUSES = Set.of("CANCELED", "INACTIVE", "HOLD");
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = createStatusTransitionRules();
    private final ReservationReceptionRepository reservationRepository;
    private final ReservationReceptionMapper reservationMyBatisMapper;
    private final ReservationResMapStruct reservationResMapStruct;
    private final ReservationReqMapStruct reservationReqMapStruct;
    private final ReservationStatusHistoryRepository reservationStatusHistoryRepository;
    private final ReservationDoctorScheduleRepository reservationDoctorScheduleRepository;
    private final ReservationTimeSlotRepository reservationTimeSlotRepository;
    private final ReservationBookingRuleRepository reservationBookingRuleRepository;
    private final PatientServiceClient patientServiceClient;
    private final AuditLogService auditLogService;
    private final ReceptionNumberSequenceClient receptionNumberSequenceClient;

    @Override
    public List<ReservationReceptionDTO> getReservationList(Map<String, Object> searchCondition) {
        String searchType = (String) searchCondition.get("searchType");
        String searchValue = (String) searchCondition.get("searchValue");
        List<ReservationReceptionDTO> list = reservationMyBatisMapper.selectReservations(searchType, searchValue);
        list.forEach(this::normalizeFrontDisplayNames);
        return list;
    }

    @Override
    @Cacheable(key = "#reservationId", value = "RESERVATION")
    public ReservationReceptionDTO getReservation(Long reservationId) {
        ReservationReceptionEntity entity = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found. reservationId=" + reservationId));
        return toDisplayDto(entity);
    }

    @Override
    public List<ReservationReceptionDTO> getAutoSyncReservations(LocalDate targetDate) {
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay();

        return reservationRepository
                .findAllByStatusAndIsActiveTrueAndReservedAtBetweenOrderByReservationIdAsc(
                        RESERVED_STATUS,
                        start,
                        end
                )
                .stream()
                .filter(reservation -> isAutoSyncTarget(reservation, targetDate))
                .map(this::toDisplayDto)
                .toList();
    }

    @Override
    @Transactional
    public void createReservation(ReservationReceptionDTO reservation) {
        String reservationNo = resolveCreateReservationNo(reservation);
        if (reservation.getReservedAt() == null) {
            throw new IllegalArgumentException("reservedAt is required");
        }
        if (reservation.getDepartmentId() == null) {
            throw new IllegalArgumentException("departmentId is required");
        }

        String normalizedStatus = normalizeStatus(defaultIfBlank(reservation.getStatus(), "RESERVED"));
        assertStatusSupported(normalizedStatus);
        String reasonCode = resolveReasonCodeForStatus(normalizedStatus, reservation, null);
        String reasonText = resolveReasonTextForStatus(normalizedStatus, reservation, null);
        validateReasonIfRequired(normalizedStatus, reasonCode, reasonText);

        ReservationReceptionEntity entity = reservationReqMapStruct.toEntity(reservation);
        entity.setReservationNo(reservationNo);
        entity.setPatientId(resolvePatientId(entity.getPatientId(), entity.getPatientName()));
        entity.setPatientName(resolvePatientName(entity.getPatientId(), entity.getPatientName()));
        entity.setDepartmentName(resolveDepartmentName(entity.getDepartmentId(), entity.getDepartmentName()));
        entity.setDoctorName(resolveDoctorName(entity.getDoctorId(), entity.getDepartmentId(), entity.getDoctorName()));
        entity.setStatus(normalizedStatus);
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }
        applyStatusSideEffects(entity, normalizedStatus, reasonCode, reasonText);

        ReservationReceptionEntity saved = reservationRepository.save(entity);
        ensureBookingRule(saved);
        ReservationDoctorScheduleEntity schedule = ensureDoctorSchedule(saved);
        ensureTimeSlot(saved, schedule);
        saveReservationStatusHistory(saved.getReservationId(), null, saved.getStatus(), reservation.getCreatedBy(), reasonText);
        ReservationReceptionDTO after = toDisplayDto(saved);

        auditLogService.log(
                "RESERVATION",
                saved.getReservationId(),
                "CREATE",
                reservation.getCreatedBy(),
                reasonCode,
                reasonText,
                null,
                after
        );
    }

    private String resolveCreateReservationNo(ReservationReceptionDTO reservation) {
        String requestedReservationNo = trimToNull(reservation.getReservationNo());
        if (requestedReservationNo != null) {
            if (reservationRepository.existsByReservationNo(requestedReservationNo)) {
                throw new IllegalArgumentException("Duplicated reservationNo: " + requestedReservationNo);
            }
            return requestedReservationNo;
        }

        String generatedReservationNo = receptionNumberSequenceClient.nextReceptionNo("RESERVATION");
        if (reservationRepository.existsByReservationNo(generatedReservationNo)) {
            throw new IllegalStateException("Generated duplicated reservationNo: " + generatedReservationNo);
        }
        return generatedReservationNo;
    }

    @Override
    @Transactional
    @CacheEvict(value = "RESERVATION", key = "#reservationId")
    public void updateReservation(Long reservationId, ReservationReceptionDTO reservation) {
        ReservationReceptionEntity existing = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found. reservationId=" + reservationId));

        ReservationReceptionDTO before = toDisplayDto(existing);
        String beforeStatus = normalizeStatus(existing.getStatus());

        if (trimToNull(reservation.getReservationNo()) != null) {
            String nextReservationNo = trimToNull(reservation.getReservationNo());
            if (!Objects.equals(existing.getReservationNo(), nextReservationNo)) {
                reservationRepository.findByReservationNo(nextReservationNo)
                        .filter(item -> !Objects.equals(item.getReservationId(), existing.getReservationId()))
                        .ifPresent(item -> {
                            throw new IllegalArgumentException("Duplicated reservationNo: " + nextReservationNo);
                        });
                existing.setReservationNo(nextReservationNo);
            }
        }
        if (reservation.getPatientId() != null) {
            existing.setPatientId(reservation.getPatientId());
        }
        if (trimToNull(reservation.getPatientName()) != null || reservation.getPatientId() != null) {
            existing.setPatientId(resolvePatientId(existing.getPatientId(), firstNonBlank(reservation.getPatientName(), existing.getPatientName())));
            existing.setPatientName(resolvePatientName(existing.getPatientId(), firstNonBlank(reservation.getPatientName(), existing.getPatientName())));
        }
        if (reservation.getDepartmentId() != null) {
            existing.setDepartmentId(reservation.getDepartmentId());
        }
        if (trimToNull(reservation.getDepartmentName()) != null || reservation.getDepartmentId() != null) {
            existing.setDepartmentName(resolveDepartmentName(existing.getDepartmentId(), firstNonBlank(reservation.getDepartmentName(), existing.getDepartmentName())));
        }
        if (reservation.getDoctorId() != null) {
            existing.setDoctorId(reservation.getDoctorId());
        }
        if (trimToNull(reservation.getDoctorName()) != null || reservation.getDoctorId() != null) {
            existing.setDoctorName(resolveDoctorName(existing.getDoctorId(), existing.getDepartmentId(), firstNonBlank(reservation.getDoctorName(), existing.getDoctorName())));
        }
        if (reservation.getReservedAt() != null) {
            existing.setReservedAt(reservation.getReservedAt());
        }
        if (reservation.getNote() != null) {
            existing.setNote(reservation.getNote());
        }
        if (reservation.getCreatedBy() != null) {
            existing.setCreatedBy(reservation.getCreatedBy());
        }
        if (reservation.getUpdatedBy() != null) {
            existing.setUpdatedBy(reservation.getUpdatedBy());
        }

        String targetStatus = normalizeStatus(defaultIfBlank(reservation.getStatus(), existing.getStatus()));
        validateStatusTransition(beforeStatus, targetStatus);
        String reasonCode = resolveReasonCodeForStatus(targetStatus, reservation, existing);
        String reasonText = resolveReasonTextForStatus(targetStatus, reservation, existing);
        validateReasonIfRequired(targetStatus, reasonCode, reasonText);
        existing.setStatus(targetStatus);
        applyStatusSideEffects(existing, targetStatus, reasonCode, reasonText);

        ReservationReceptionEntity saved = reservationRepository.save(existing);
        ensureBookingRule(saved);
        ReservationDoctorScheduleEntity schedule = ensureDoctorSchedule(saved);
        ensureTimeSlot(saved, schedule);
        if (!Objects.equals(beforeStatus, targetStatus)) {
            saveReservationStatusHistory(
                    saved.getReservationId(),
                    beforeStatus,
                    targetStatus,
                    reservation.getUpdatedBy(),
                    reasonText
            );
        }
        ReservationReceptionDTO after = toDisplayDto(saved);

        auditLogService.log(
                "RESERVATION",
                saved.getReservationId(),
                "UPDATE",
                reservation.getUpdatedBy(),
                reasonCode,
                reasonText,
                before,
                after
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "RESERVATION", key = "#reservationId")
    public ReservationReceptionDTO updateReservationStatus(Long reservationId, String status, Long changedBy, String reasonCode, String reasonText) {
        return doUpdateReservationStatus(reservationId, status, changedBy, reasonCode, reasonText);
    }

    @Override
    @Transactional
    @CacheEvict(value = "RESERVATION", key = "#reservationId")
    public ReservationReceptionDTO completeReservationByAutoSync(Long reservationId) {
        return doUpdateReservationStatus(
                reservationId,
                COMPLETED_STATUS,
                AUTO_SYNC_SYSTEM_USER_ID,
                AUTO_SYNC_REASON_CODE,
                AUTO_SYNC_REASON_TEXT
        );
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

        ReservationReceptionDTO before = toDisplayDto(existing);
        String beforeStatus = normalizeStatus(existing.getStatus());
        String targetStatus = normalizeStatus(status);
        validateStatusTransition(beforeStatus, targetStatus);

        String normalizedReasonCode = firstNonBlank(trimToNull(reasonCode), resolveReasonCodeForStatus(targetStatus, null, existing));
        String normalizedReasonText = firstNonBlank(trimToNull(reasonText), resolveReasonTextForStatus(targetStatus, null, existing));
        validateReasonIfRequired(targetStatus, normalizedReasonCode, normalizedReasonText);

        existing.setStatus(targetStatus);
        existing.setUpdatedBy(changedBy);
        applyStatusSideEffects(existing, targetStatus, normalizedReasonCode, normalizedReasonText);

        ReservationReceptionEntity saved = reservationRepository.save(existing);
        ensureBookingRule(saved);
        ReservationDoctorScheduleEntity schedule = ensureDoctorSchedule(saved);
        ensureTimeSlot(saved, schedule);
        saveReservationStatusHistory(saved.getReservationId(), beforeStatus, targetStatus, changedBy, normalizedReasonText);
        ReservationReceptionDTO after = toDisplayDto(saved);

        auditLogService.log(
                "RESERVATION",
                saved.getReservationId(),
                "STATUS_CHANGE",
                changedBy,
                normalizedReasonCode,
                normalizedReasonText,
                before,
                after
        );

        return after;
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
        ReservationTimeSlotEntity timeSlot = reservationTimeSlotRepository
                .findTopByReservationIdOrderByTimeSlotIdDesc(reservation.getReservationId())
                .orElseGet(ReservationTimeSlotEntity::new);

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
        String normalized = normalizeStatus(defaultIfBlank(reservationStatus, "RESERVED"));
        if ("CANCELED".equals(normalized)) {
            return "CANCELED";
        }
        if ("COMPLETED".equals(normalized)) {
            return "COMPLETED";
        }
        return "RESERVED";
    }

    private void validateStatusTransition(String fromStatus, String toStatus) {
        String normalizedFrom = normalizeStatus(defaultIfBlank(fromStatus, "RESERVED"));
        String normalizedTo = normalizeStatus(toStatus);
        assertStatusSupported(normalizedFrom);
        assertStatusSupported(normalizedTo);

        if (Objects.equals(normalizedFrom, normalizedTo)) {
            return;
        }

        Set<String> allowedTargets = STATUS_TRANSITIONS.getOrDefault(normalizedFrom, Collections.emptySet());
        if (!allowedTargets.contains(normalizedTo)) {
            throw new IllegalArgumentException("Invalid status transition: " + normalizedFrom + " -> " + normalizedTo);
        }
    }

    private void assertStatusSupported(String status) {
        if (!STATUS_TRANSITIONS.containsKey(status)) {
            throw new IllegalArgumentException("Unsupported status: " + status);
        }
    }

    private void validateReasonIfRequired(String status, String reasonCode, String reasonText) {
        if (!REASON_REQUIRED_STATUSES.contains(status)) {
            return;
        }
        if (trimToNull(reasonCode) == null && trimToNull(reasonText) == null) {
            throw new IllegalArgumentException("Reason is required for status: " + status);
        }
    }

    private void applyStatusSideEffects(ReservationReceptionEntity entity, String status, String reasonCode, String reasonText) {
        String normalizedStatus = normalizeStatus(status);
        String normalizedReasonCode = trimToNull(reasonCode);
        String normalizedReasonText = trimToNull(reasonText);

        switch (normalizedStatus) {
            case "CANCELED" -> {
                entity.setIsActive(false);
                entity.setCanceledAt(LocalDateTime.now());
                entity.setCancelReasonCode(normalizedReasonCode);
                entity.setCancelReasonText(normalizedReasonText);
                entity.setInactiveAt(LocalDateTime.now());
                entity.setInactiveReasonCode(normalizedReasonCode);
                entity.setInactiveReasonText(normalizedReasonText);
            }
            case "INACTIVE" -> {
                entity.setIsActive(false);
                entity.setInactiveAt(LocalDateTime.now());
                entity.setInactiveReasonCode(normalizedReasonCode);
                entity.setInactiveReasonText(normalizedReasonText);
            }
            case "HOLD" -> {
                entity.setIsActive(true);
                entity.setInactiveAt(null);
                entity.setInactiveReasonCode(null);
                entity.setInactiveReasonText(null);
                entity.setCanceledAt(null);
                entity.setCancelReasonCode(null);
                entity.setCancelReasonText(null);
            }
            default -> {
                entity.setIsActive(true);
                entity.setInactiveAt(null);
                entity.setInactiveReasonCode(null);
                entity.setInactiveReasonText(null);
                entity.setCanceledAt(null);
                entity.setCancelReasonCode(null);
                entity.setCancelReasonText(null);
            }
        }
    }

    private String normalizeStatus(String status) {
        String normalized = trimToNull(status);
        if (normalized == null) {
            throw new IllegalArgumentException("status is required");
        }
        normalized = normalized.toUpperCase();
        return switch (normalized) {
            case "CANCELLED", "CANCELED" -> "CANCELED";
            case "DONE" -> "COMPLETED";
            case "ON_HOLD" -> "HOLD";
            case "TRANSFERRED" -> "INACTIVE";
            default -> normalized;
        };
    }

    private static Map<String, Set<String>> createStatusTransitionRules() {
        Map<String, Set<String>> rules = new HashMap<>();
        rules.put("RESERVED", Set.of("WAITING", "IN_PROGRESS", "HOLD", "CANCELED", "INACTIVE", "COMPLETED"));
        rules.put("WAITING", Set.of("RESERVED", "IN_PROGRESS", "HOLD", "CANCELED", "INACTIVE", "COMPLETED"));
        rules.put("IN_PROGRESS", Set.of("HOLD", "CANCELED", "INACTIVE", "COMPLETED"));
        rules.put("HOLD", Set.of("WAITING", "RESERVED", "IN_PROGRESS", "CANCELED", "INACTIVE"));
        rules.put("PAYMENT_WAIT", Set.of("COMPLETED", "CANCELED", "INACTIVE"));
        rules.put("COMPLETED", Collections.emptySet());
        rules.put("CANCELED", Collections.emptySet());
        rules.put("INACTIVE", Collections.emptySet());
        return rules;
    }

    private String resolveReasonCodeForStatus(String status, ReservationReceptionDTO request, ReservationReceptionEntity existing) {
        if ("CANCELED".equals(status)) {
            return firstNonBlank(
                    request == null ? null : request.getCancelReasonCode(),
                    existing == null ? null : existing.getCancelReasonCode(),
                    existing == null ? null : existing.getInactiveReasonCode(),
                    "USER_CANCEL"
            );
        }
        if ("INACTIVE".equals(status)) {
            return firstNonBlank(
                    request == null ? null : request.getInactiveReasonCode(),
                    existing == null ? null : existing.getInactiveReasonCode(),
                    "INACTIVE_BY_USER"
            );
        }
        if ("HOLD".equals(status)) {
            return firstNonBlank("ON_HOLD");
        }
        return null;
    }

    private String resolveReasonTextForStatus(String status, ReservationReceptionDTO request, ReservationReceptionEntity existing) {
        if ("CANCELED".equals(status)) {
            return firstNonBlank(
                    request == null ? null : request.getCancelReasonText(),
                    existing == null ? null : existing.getCancelReasonText(),
                    existing == null ? null : existing.getInactiveReasonText()
            );
        }
        if ("INACTIVE".equals(status)) {
            return firstNonBlank(
                    request == null ? null : request.getInactiveReasonText(),
                    existing == null ? null : existing.getInactiveReasonText()
            );
        }
        if ("HOLD".equals(status)) {
            return firstNonBlank(request == null ? null : request.getNote(), existing == null ? null : existing.getNote());
        }
        return null;
    }

    private boolean isAutoSyncTarget(ReservationReceptionEntity reservation, LocalDate targetDate) {
        if (reservation == null) {
            return false;
        }
        if (!RESERVED_STATUS.equals(reservation.getStatus())) {
            return false;
        }
        if (!Boolean.TRUE.equals(reservation.getIsActive())) {
            return false;
        }
        LocalDateTime reservedAt = reservation.getReservedAt();
        return reservedAt != null && targetDate.equals(reservedAt.toLocalDate());
    }

    private ReservationReceptionDTO toDisplayDto(ReservationReceptionEntity entity) {
        if (entity == null) {
            return null;
        }
        ReservationReceptionDTO mapped = reservationMyBatisMapper.selectReservationById(entity.getReservationId());
        if (mapped != null) {
            normalizeFrontDisplayNames(mapped);
            return mapped;
        }
        enrichDisplayNames(entity);
        ReservationReceptionDTO fallback = reservationResMapStruct.toDto(entity);
        normalizeFrontDisplayNames(fallback);
        return fallback;
    }

    private void normalizeFrontDisplayNames(ReservationReceptionDTO dto) {
        if (dto == null) {
            return;
        }
        if (dto.getDepartmentId() != null) {
            dto.setDepartmentName(resolveDepartmentName(dto.getDepartmentId(), dto.getDepartmentName()));
        }
        if (dto.getDoctorId() != null) {
            dto.setDoctorName(resolveDoctorName(dto.getDoctorId(), dto.getDepartmentId(), dto.getDoctorName()));
        }
    }

    private void enrichDisplayNames(ReservationReceptionEntity reservation) {
        if (reservation == null) {
            return;
        }

        if (reservation.getPatientId() != null) {
            try {
                reservation.setPatientName(resolvePatientName(reservation.getPatientId(), reservation.getPatientName()));
            } catch (RuntimeException ignored) {
                if (trimToNull(reservation.getPatientName()) == null) {
                    reservation.setPatientName("PATIENT-" + reservation.getPatientId());
                }
            }
        }

        try {
            reservation.setDepartmentName(resolveDepartmentName(reservation.getDepartmentId(), reservation.getDepartmentName()));
        } catch (RuntimeException ignored) {
            // keep current value when department metadata cannot be resolved
        }

        try {
            reservation.setDoctorName(resolveDoctorName(reservation.getDoctorId(), reservation.getDepartmentId(), reservation.getDoctorName()));
        } catch (RuntimeException ignored) {
            // keep current value when doctor metadata cannot be resolved
        }
    }

    private Long resolvePatientId(Long patientId, String patientName) {
        if (patientId != null) {
            return patientServiceClient.requirePatientById(patientId).patientId();
        }
        String normalizedPatientName = trimToNull(patientName);
        if (normalizedPatientName == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        return patientServiceClient.requirePatientByName(normalizedPatientName).patientId();
    }

    private String resolvePatientName(Long patientId, String fallbackName) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        PatientServiceClient.PatientSummary patientSummary = patientServiceClient.requirePatientById(patientId);
        String resolvedName = firstNonBlank(patientSummary.patientName(), fallbackName);
        if (resolvedName == null) {
            throw new IllegalArgumentException("patientName not found for patientId=" + patientId);
        }
        return resolvedName;
    }

    private String resolveDepartmentName(String departmentId, String fallbackName) {
        String normalizedName = trimToNull(fallbackName);
        if (normalizedName != null) {
            return normalizedName;
        }

        if (trimToNull(departmentId) != null) {
            throw new IllegalArgumentException("departmentName is required when departmentId is provided.");
        }

        return null;
    }

    private String resolveDoctorName(String doctorId, String departmentId, String fallbackName) {
        String normalizedDoctorId = trimToNull(doctorId);
        if (normalizedDoctorId == null) {
            return null;
        }

        String normalizedName = trimToNull(fallbackName);
        if (normalizedName != null) {
            return normalizedName;
        }

        throw new IllegalArgumentException("doctorName is required when doctorId is provided.");
    }
    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String normalized = trimToNull(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return trimToNull(value) != null ? trimToNull(value) : defaultValue;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

