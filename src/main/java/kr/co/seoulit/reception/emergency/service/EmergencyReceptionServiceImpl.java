package kr.co.seoulit.reception.emergency.service;

import kr.co.seoulit.common.sequence.ReceptionNumberSequenceClient;
import kr.co.seoulit.reception.emergency.dto.EmergencyReceptionDTO;
import kr.co.seoulit.reception.emergency.entity.EmergencyReceptionEntity;
import kr.co.seoulit.reception.emergency.entity.EmergencyTriageEntity;
import kr.co.seoulit.reception.emergency.mapper.EmergencyReceptionMapper;
import kr.co.seoulit.reception.emergency.repository.EmergencyReceptionRepository;
import kr.co.seoulit.reception.emergency.repository.EmergencyTriageRepository;
import kr.co.seoulit.reception.outpatient.entity.OutpatientReceptionEntity;
import kr.co.seoulit.common.client.PatientServiceClient;
import kr.co.seoulit.reception.outpatient.repository.OutpatientReceptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class EmergencyReceptionServiceImpl implements EmergencyReceptionService {

    private static final Set<String> REASON_REQUIRED_STATUSES = Set.of("CANCELLED", "INACTIVE", "HOLD");
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = createStatusTransitionRules();

    private final OutpatientReceptionRepository receptionRepository;
    private final EmergencyReceptionRepository emergencyRepository;
    private final EmergencyTriageRepository emergencyTriageRepository;
    private final EmergencyReceptionMapper emergencyMyBatisMapper;
    private final PatientServiceClient patientServiceClient;
    private final ReceptionNumberSequenceClient receptionNumberSequenceClient;

    @Override
    public List<EmergencyReceptionDTO> getEmergencyReceptionList(Map<String, Object> searchCondition) {
        String searchType = (String) searchCondition.get("searchType");
        String searchValue = (String) searchCondition.get("searchValue");
        return emergencyMyBatisMapper.selectEmergencyReceptions(searchType, searchValue)
                .stream()
                .map(this::normalizeStatusForResponse)
                .toList();
    }

    @Override
    public EmergencyReceptionDTO getEmergencyReception(Long receptionId) {
        OutpatientReceptionEntity reception = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("Emergency reception not found. receptionId=" + receptionId));
        EmergencyReceptionEntity emergency = emergencyRepository.findByReceptionId(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("Emergency detail not found. receptionId=" + receptionId));
        EmergencyTriageEntity triage = emergencyTriageRepository
                .findTopByReceptionIdOrderByTriageDatetimeDesc(receptionId)
                .orElse(null);

        enrichDisplayNames(reception);
        reception.setStatus(normalizeStatus(reception.getStatus()));
        return toEmergencyDto(reception, emergency, triage);
    }

    @Override
    @Transactional
    public void createEmergencyReception(EmergencyReceptionDTO request) {
        String receptionNo = resolveCreateReceptionNo(request);
        if (request.getPatientId() == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        if (request.getDepartmentId() == null) {
            throw new IllegalArgumentException("departmentId is required");
        }
        if (request.getTriageLevel() == null || trimToNull(request.getChiefComplaint()) == null) {
            throw new IllegalArgumentException("triageLevel and chiefComplaint are required");
        }

        String normalizedStatus = normalizeStatus(defaultIfBlank(request.getStatus(), "WAITING"));
        assertStatusSupported(normalizedStatus);
        String reasonCode = resolveReasonCodeForStatus(normalizedStatus, request, null);
        String reasonText = resolveReasonTextForStatus(normalizedStatus, request, null);
        validateReasonIfRequired(normalizedStatus, reasonCode, reasonText);

        OutpatientReceptionEntity reception = new OutpatientReceptionEntity();
        reception.setReceptionNo(receptionNo);
        reception.setPatientId(request.getPatientId());
        reception.setPatientName(resolvePatientName(request.getPatientId(), request.getPatientName()));
        reception.setVisitType("EMERGENCY");
        reception.setDepartmentId(request.getDepartmentId());
        reception.setDepartmentName(resolveDepartmentName(request.getDepartmentId(), request.getDepartmentName()));
        reception.setDoctorId(request.getDoctorId());
        reception.setDoctorName(resolveDoctorName(request.getDoctorId(), request.getDoctorName()));
        reception.setReservationId(request.getReservationId());
        reception.setScheduledAt(request.getScheduledAt());
        reception.setArrivedAt(request.getArrivedAt());
        reception.setStatus(normalizedStatus);
        reception.setNote(request.getNote());
        reception.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        applyStatusSideEffects(reception, normalizedStatus, reasonCode, reasonText);

        OutpatientReceptionEntity saved = receptionRepository.save(reception);

        EmergencyReceptionEntity emergency = new EmergencyReceptionEntity();
        emergency.setReceptionId(saved.getReceptionId());
        emergency.setChiefComplaint(request.getChiefComplaint());
        emergency.setVitalTemp(request.getVitalTemp());
        emergency.setBloodPressure(toBloodPressure(request.getVitalBpSystolic(), request.getVitalBpDiastolic()));
        emergency.setVitalHr(request.getVitalHr());
        emergency.setArrivalMode(request.getArrivalMode());
        emergency.setArrivalDatetime(request.getArrivedAt());
        emergency.setActiveYn(toYn(saved.getIsActive()));
        emergencyRepository.save(emergency);

        EmergencyTriageEntity triage = new EmergencyTriageEntity();
        triage.setReceptionId(saved.getReceptionId());
        triage.setTriageLevelCd(toTriageLevelCode(request.getTriageLevel()));
        triage.setTriageDatetime(request.getArrivedAt() != null ? request.getArrivedAt() : LocalDateTime.now());
        triage.setTriageNote(request.getTriageNote());
        triage.setActiveYn(toYn(saved.getIsActive()));
        emergencyTriageRepository.save(triage);
    }

    private String resolveCreateReceptionNo(EmergencyReceptionDTO request) {
        String requestedReceptionNo = trimToNull(request.getReceptionNo());
        if (requestedReceptionNo != null) {
            if (receptionRepository.existsByReceptionNo(requestedReceptionNo)) {
                throw new IllegalArgumentException("Duplicated receptionNo: " + requestedReceptionNo);
            }
            return requestedReceptionNo;
        }

        String generatedReceptionNo = receptionNumberSequenceClient.nextReceptionNo("EMERGENCY");
        if (receptionRepository.existsByReceptionNo(generatedReceptionNo)) {
            throw new IllegalStateException("Generated duplicated receptionNo: " + generatedReceptionNo);
        }
        return generatedReceptionNo;
    }

    @Override
    @Transactional
    public void updateEmergencyReception(Long receptionId, EmergencyReceptionDTO request) {
        OutpatientReceptionEntity reception = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("Emergency reception not found. receptionId=" + receptionId));
        EmergencyReceptionEntity emergency = emergencyRepository.findByReceptionId(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("Emergency detail not found. receptionId=" + receptionId));
        EmergencyTriageEntity triage = emergencyTriageRepository
                .findTopByReceptionIdOrderByTriageDatetimeDesc(receptionId)
                .orElse(null);

        if (trimToNull(request.getReceptionNo()) != null) {
            String nextReceptionNo = trimToNull(request.getReceptionNo());
            if (!Objects.equals(reception.getReceptionNo(), nextReceptionNo)) {
                receptionRepository.findByReceptionNo(nextReceptionNo)
                        .filter(item -> !Objects.equals(item.getReceptionId(), reception.getReceptionId()))
                        .ifPresent(item -> {
                            throw new IllegalArgumentException("Duplicated receptionNo: " + nextReceptionNo);
                        });
                reception.setReceptionNo(nextReceptionNo);
            }
        }
        if (request.getPatientId() != null) {
            reception.setPatientId(request.getPatientId());
        }
        if (trimToNull(request.getPatientName()) != null || request.getPatientId() != null) {
            reception.setPatientName(resolvePatientName(reception.getPatientId(), firstNonBlank(request.getPatientName(), reception.getPatientName())));
        }
        if (request.getDepartmentId() != null) {
            reception.setDepartmentId(request.getDepartmentId());
        }
        if (trimToNull(request.getDepartmentName()) != null || request.getDepartmentId() != null) {
            reception.setDepartmentName(resolveDepartmentName(reception.getDepartmentId(), firstNonBlank(request.getDepartmentName(), reception.getDepartmentName())));
        }
        if (request.getDoctorId() != null) {
            reception.setDoctorId(request.getDoctorId());
        }
        if (trimToNull(request.getDoctorName()) != null || request.getDoctorId() != null) {
            reception.setDoctorName(resolveDoctorName(reception.getDoctorId(), firstNonBlank(request.getDoctorName(), reception.getDoctorName())));
        }
        if (request.getReservationId() != null) {
            reception.setReservationId(request.getReservationId());
        }
        if (request.getScheduledAt() != null) {
            reception.setScheduledAt(request.getScheduledAt());
        }
        if (request.getArrivedAt() != null) {
            reception.setArrivedAt(request.getArrivedAt());
        }
        if (request.getNote() != null) {
            reception.setNote(request.getNote());
        }

        String fromStatus = normalizeStatus(reception.getStatus());
        String targetStatus = normalizeStatus(defaultIfBlank(request.getStatus(), reception.getStatus()));
        validateStatusTransition(fromStatus, targetStatus);
        String reasonCode = resolveReasonCodeForStatus(targetStatus, request, reception);
        String reasonText = resolveReasonTextForStatus(targetStatus, request, reception);
        validateReasonIfRequired(targetStatus, reasonCode, reasonText);
        reception.setStatus(targetStatus);
        applyStatusSideEffects(reception, targetStatus, reasonCode, reasonText);

        if (request.getChiefComplaint() != null) {
            emergency.setChiefComplaint(request.getChiefComplaint());
        }
        if (request.getVitalTemp() != null) {
            emergency.setVitalTemp(request.getVitalTemp());
        }
        if (request.getVitalBpSystolic() != null || request.getVitalBpDiastolic() != null) {
            Integer[] bp = parseBloodPressure(emergency.getBloodPressure());
            Integer systolic = request.getVitalBpSystolic() != null ? request.getVitalBpSystolic() : bp[0];
            Integer diastolic = request.getVitalBpDiastolic() != null ? request.getVitalBpDiastolic() : bp[1];
            emergency.setBloodPressure(toBloodPressure(systolic, diastolic));
        }
        if (request.getVitalHr() != null) {
            emergency.setVitalHr(request.getVitalHr());
        }
        if (request.getArrivalMode() != null) {
            emergency.setArrivalMode(request.getArrivalMode());
        }
        if (request.getArrivedAt() != null) {
            emergency.setArrivalDatetime(request.getArrivedAt());
        }
        emergency.setActiveYn(toYn(reception.getIsActive()));

        receptionRepository.save(reception);
        emergencyRepository.save(emergency);

        if (request.getTriageLevel() != null || request.getTriageNote() != null) {
            if (triage == null) {
                triage = new EmergencyTriageEntity();
                triage.setReceptionId(receptionId);
            }
            if (request.getTriageLevel() != null) {
                triage.setTriageLevelCd(toTriageLevelCode(request.getTriageLevel()));
            } else if (triage.getTriageLevelCd() == null) {
                triage.setTriageLevelCd("3");
            }
            if (request.getTriageNote() != null) {
                triage.setTriageNote(request.getTriageNote());
            }
            triage.setTriageDatetime(request.getArrivedAt() != null ? request.getArrivedAt() : LocalDateTime.now());
            triage.setActiveYn(toYn(reception.getIsActive()));
            emergencyTriageRepository.save(triage);
        }
    }

    private EmergencyReceptionDTO toEmergencyDto(
            OutpatientReceptionEntity reception,
            EmergencyReceptionEntity emergency,
            EmergencyTriageEntity triage
    ) {
        EmergencyReceptionDTO dto = new EmergencyReceptionDTO();
        dto.setReceptionId(reception.getReceptionId());
        dto.setReceptionNo(reception.getReceptionNo());
        dto.setPatientId(reception.getPatientId());
        dto.setPatientName(reception.getPatientName());
        dto.setVisitType(reception.getVisitType());
        dto.setDepartmentId(reception.getDepartmentId());
        dto.setDepartmentName(reception.getDepartmentName());
        dto.setDoctorId(reception.getDoctorId());
        dto.setDoctorName(reception.getDoctorName());
        dto.setReservationId(reception.getReservationId());
        dto.setScheduledAt(reception.getScheduledAt());
        dto.setArrivedAt(reception.getArrivedAt());
        dto.setStatus(normalizeStatus(reception.getStatus()));
        dto.setNote(reception.getNote());
        dto.setIsActive(reception.getIsActive());
        dto.setCreatedAt(reception.getCreatedAt());
        dto.setUpdatedAt(reception.getUpdatedAt());
        dto.setTriageLevel(toTriageLevel(triage != null ? triage.getTriageLevelCd() : null));
        dto.setChiefComplaint(emergency.getChiefComplaint());
        dto.setVitalTemp(emergency.getVitalTemp());
        Integer[] bp = parseBloodPressure(emergency.getBloodPressure());
        dto.setVitalBpSystolic(bp[0]);
        dto.setVitalBpDiastolic(bp[1]);
        dto.setVitalHr(emergency.getVitalHr());
        dto.setVitalRr(emergency.getVitalRr());
        dto.setVitalSpo2(emergency.getVitalSpo2());
        dto.setArrivalMode(emergency.getArrivalMode());
        dto.setTriageNote(triage != null ? triage.getTriageNote() : null);
        return dto;
    }

    private EmergencyReceptionDTO normalizeStatusForResponse(EmergencyReceptionDTO dto) {
        if (dto == null) {
            return null;
        }
        dto.setStatus(normalizeStatus(dto.getStatus()));
        return dto;
    }

    private void validateStatusTransition(String fromStatus, String toStatus) {
        String normalizedFrom = normalizeStatus(defaultIfBlank(fromStatus, "WAITING"));
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

    private void applyStatusSideEffects(OutpatientReceptionEntity reception, String status, String reasonCode, String reasonText) {
        String normalizedStatus = normalizeStatus(status);
        String normalizedReasonCode = trimToNull(reasonCode);
        String normalizedReasonText = trimToNull(reasonText);

        switch (normalizedStatus) {
            case "CANCELLED" -> {
                reception.setIsActive(false);
                reception.setInactiveAt(LocalDateTime.now());
                reception.setCancelReasonCode(normalizedReasonCode);
                reception.setCancelReasonText(normalizedReasonText);
                reception.setInactiveReasonCode(normalizedReasonCode);
                reception.setInactiveReasonText(normalizedReasonText);
                reception.setHoldReasonCode(null);
                reception.setHoldReasonText(null);
            }
            case "INACTIVE" -> {
                reception.setIsActive(false);
                reception.setInactiveAt(LocalDateTime.now());
                reception.setInactiveReasonCode(normalizedReasonCode);
                reception.setInactiveReasonText(normalizedReasonText);
                reception.setCancelReasonCode(null);
                reception.setCancelReasonText(null);
                reception.setHoldReasonCode(null);
                reception.setHoldReasonText(null);
            }
            case "HOLD" -> {
                reception.setIsActive(true);
                reception.setInactiveAt(null);
                reception.setInactiveReasonCode(null);
                reception.setInactiveReasonText(null);
                reception.setCancelReasonCode(null);
                reception.setCancelReasonText(null);
                reception.setHoldReasonCode(normalizedReasonCode);
                reception.setHoldReasonText(normalizedReasonText);
            }
            default -> {
                reception.setIsActive(true);
                reception.setInactiveAt(null);
                reception.setInactiveReasonCode(null);
                reception.setInactiveReasonText(null);
                reception.setCancelReasonCode(null);
                reception.setCancelReasonText(null);
                reception.setHoldReasonCode(null);
                reception.setHoldReasonText(null);
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
            case "CANCELED" -> "CANCELLED";
            case "CALLED", "호출" -> "WAITING";
            case "REGISTERED" -> "WAITING";
            case "DONE" -> "COMPLETED";
            case "ON_HOLD" -> "HOLD";
            case "OBSERVING" -> "OBSERVATION";
            case "TRANSFERRED" -> "INACTIVE";
            case "TRIAGE_PROGRESS" -> "TRIAGE_IN_PROGRESS";
            default -> normalized;
        };
    }

    private static Map<String, Set<String>> createStatusTransitionRules() {
        Map<String, Set<String>> rules = new HashMap<>();
        rules.put("RESERVED", Set.of("WAITING", "CANCELLED", "INACTIVE"));
        rules.put("WAITING", Set.of("TRIAGE", "TRIAGE_IN_PROGRESS", "IN_PROGRESS", "HOLD", "COMPLETED", "CANCELLED", "INACTIVE"));
        rules.put("TRIAGE", Set.of("TRIAGE_IN_PROGRESS", "IN_PROGRESS", "HOLD", "CANCELLED", "INACTIVE"));
        rules.put("TRIAGE_IN_PROGRESS", Set.of("IN_PROGRESS", "HOLD", "CANCELLED", "INACTIVE"));
        rules.put("IN_PROGRESS", Set.of("OBSERVATION", "HOLD", "PAYMENT_WAIT", "COMPLETED", "CANCELLED", "INACTIVE"));
        rules.put("OBSERVATION", Set.of("IN_PROGRESS", "HOLD", "PAYMENT_WAIT", "COMPLETED", "CANCELLED", "INACTIVE"));
        rules.put("HOLD", Set.of("WAITING", "TRIAGE", "TRIAGE_IN_PROGRESS", "IN_PROGRESS", "CANCELLED", "INACTIVE"));
        rules.put("PAYMENT_WAIT", Set.of("COMPLETED", "CANCELLED", "INACTIVE"));
        rules.put("COMPLETED", Set.of("CANCELLED"));
        rules.put("CANCELLED", Collections.emptySet());
        rules.put("INACTIVE", Collections.emptySet());
        return rules;
    }

    private String resolveReasonCodeForStatus(String status, EmergencyReceptionDTO request, OutpatientReceptionEntity existing) {
        if ("CANCELLED".equals(status)) {
            return firstNonBlank(
                    trimToNull(request == null ? null : request.getNote()) != null ? "CANCELLED_BY_USER" : null,
                    existing == null ? null : existing.getCancelReasonCode(),
                    existing == null ? null : existing.getInactiveReasonCode(),
                    "CANCELLED_BY_USER"
            );
        }
        if ("INACTIVE".equals(status)) {
            return firstNonBlank(
                    trimToNull(request == null ? null : request.getNote()) != null ? "INACTIVE_BY_USER" : null,
                    existing == null ? null : existing.getInactiveReasonCode()
            );
        }
        if ("HOLD".equals(status)) {
            return firstNonBlank(
                    trimToNull(request == null ? null : request.getNote()) != null ? "ON_HOLD" : null,
                    existing == null ? null : existing.getHoldReasonCode()
            );
        }
        return null;
    }

    private String resolveReasonTextForStatus(String status, EmergencyReceptionDTO request, OutpatientReceptionEntity existing) {
        if ("CANCELLED".equals(status)) {
            return firstNonBlank(
                    request == null ? null : request.getNote(),
                    existing == null ? null : existing.getCancelReasonText(),
                    existing == null ? null : existing.getInactiveReasonText()
            );
        }
        if ("INACTIVE".equals(status)) {
            return firstNonBlank(
                    request == null ? null : request.getNote(),
                    existing == null ? null : existing.getInactiveReasonText()
            );
        }
        if ("HOLD".equals(status)) {
            return firstNonBlank(
                    request == null ? null : request.getNote(),
                    existing == null ? null : existing.getHoldReasonText()
            );
        }
        return null;
    }

    private String resolvePatientName(Long patientId, String fallback) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        PatientServiceClient.PatientSummary patientSummary = patientServiceClient.requirePatientById(patientId);
        String resolvedName = firstNonBlank(patientSummary.patientName(), fallback);
        if (resolvedName == null) {
            throw new IllegalArgumentException("patientName not found for patientId=" + patientId);
        }
        return resolvedName;
    }

    private String resolveDepartmentName(String departmentId, String fallback) {
        String normalizedFallback = trimToNull(fallback);
        if (normalizedFallback != null) {
            return normalizedFallback;
        }
        if (trimToNull(departmentId) != null) {
            throw new IllegalArgumentException("departmentName is required when departmentId is provided.");
        }
        return null;
    }

    private String resolveDoctorName(String doctorId, String fallback) {
        if (trimToNull(doctorId) == null) {
            return null;
        }
        String normalizedFallback = trimToNull(fallback);
        if (normalizedFallback != null) {
            return normalizedFallback;
        }
        throw new IllegalArgumentException("doctorName is required when doctorId is provided.");
    }

    private void enrichDisplayNames(OutpatientReceptionEntity reception) {
        if (reception == null) {
            return;
        }

        if (reception.getPatientId() != null) {
            try {
                reception.setPatientName(resolvePatientName(reception.getPatientId(), reception.getPatientName()));
            } catch (RuntimeException ignored) {
                if (trimToNull(reception.getPatientName()) == null) {
                    reception.setPatientName("PATIENT-" + reception.getPatientId());
                }
            }
        }

        try {
            reception.setDepartmentName(resolveDepartmentName(reception.getDepartmentId(), reception.getDepartmentName()));
        } catch (RuntimeException ignored) {
            // keep current value
        }

        try {
            reception.setDoctorName(resolveDoctorName(reception.getDoctorId(), reception.getDoctorName()));
        } catch (RuntimeException ignored) {
            // keep current value
        }
    }

    private String toYn(Boolean value) {
        if (value == null) {
            return "Y";
        }
        return value ? "Y" : "N";
    }

    private Integer[] parseBloodPressure(String bloodPressure) {
        Integer systolic = null;
        Integer diastolic = null;
        if (bloodPressure != null && bloodPressure.contains("/")) {
            String[] parts = bloodPressure.split("/");
            if (parts.length == 2) {
                try {
                    systolic = Integer.valueOf(parts[0].trim());
                } catch (NumberFormatException ignored) {
                    systolic = null;
                }
                try {
                    diastolic = Integer.valueOf(parts[1].trim());
                } catch (NumberFormatException ignored) {
                    diastolic = null;
                }
            }
        }
        return new Integer[]{systolic, diastolic};
    }

    private String toBloodPressure(Integer systolic, Integer diastolic) {
        if (systolic == null || diastolic == null) {
            return null;
        }
        return systolic + "/" + diastolic;
    }

    private String toTriageLevelCode(Integer triageLevel) {
        if (triageLevel == null) {
            return "3";
        }
        return String.valueOf(triageLevel);
    }

    private Integer toTriageLevel(String triageLevelCode) {
        if (triageLevelCode == null || triageLevelCode.isBlank()) {
            return null;
        }
        String digits = triageLevelCode.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(digits);
        } catch (NumberFormatException ignored) {
            return null;
        }
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
