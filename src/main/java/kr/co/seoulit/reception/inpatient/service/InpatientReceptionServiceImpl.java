package kr.co.seoulit.reception.inpatient.service;

import kr.co.seoulit.common.sequence.ReceptionNumberSequenceClient;
import kr.co.seoulit.reception.inpatient.dto.InpatientReceptionDTO;
import kr.co.seoulit.reception.inpatient.entity.InpatientAdmissionAuditEntity;
import kr.co.seoulit.reception.inpatient.entity.InpatientAdmissionDecisionEntity;
import kr.co.seoulit.reception.inpatient.entity.InpatientBedAssignmentEntity;
import kr.co.seoulit.reception.inpatient.entity.InpatientBedAssignmentHistoryEntity;
import kr.co.seoulit.reception.inpatient.entity.InpatientReceptionEntity;
import kr.co.seoulit.reception.inpatient.mapper.InpatientReceptionMapper;
import kr.co.seoulit.reception.inpatient.repository.InpatientAdmissionAuditRepository;
import kr.co.seoulit.reception.inpatient.repository.InpatientAdmissionDecisionRepository;
import kr.co.seoulit.reception.inpatient.repository.InpatientBedAssignmentHistoryRepository;
import kr.co.seoulit.reception.inpatient.repository.InpatientBedAssignmentRepository;
import kr.co.seoulit.reception.inpatient.repository.InpatientReceptionRepository;
import kr.co.seoulit.common.client.PatientServiceClient;
import kr.co.seoulit.reception.outpatient.entity.OutpatientReceptionEntity;
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
public class InpatientReceptionServiceImpl implements InpatientReceptionService {

    private static final Set<String> REASON_REQUIRED_STATUSES = Set.of("CANCELLED", "INACTIVE", "HOLD");
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = createStatusTransitionRules();

    private final OutpatientReceptionRepository receptionRepository;
    private final InpatientReceptionRepository inpatientRepository;
    private final InpatientBedAssignmentRepository inpatientBedAssignmentRepository;
    private final InpatientAdmissionDecisionRepository inpatientAdmissionDecisionRepository;
    private final InpatientBedAssignmentHistoryRepository inpatientBedAssignmentHistoryRepository;
    private final InpatientAdmissionAuditRepository inpatientAdmissionAuditRepository;
    private final InpatientReceptionMapper inpatientMyBatisMapper;
    private final PatientServiceClient patientServiceClient;
    private final ReceptionNumberSequenceClient receptionNumberSequenceClient;

    @Override
    public List<InpatientReceptionDTO> getInpatientReceptionList(Map<String, Object> searchCondition) {
        String searchType = (String) searchCondition.get("searchType");
        String searchValue = (String) searchCondition.get("searchValue");
        return inpatientMyBatisMapper.selectInpatientReceptions(searchType, searchValue)
                .stream()
                .map(this::normalizeStatusForResponse)
                .toList();
    }

    @Override
    public InpatientReceptionDTO getInpatientReception(Long receptionId) {
        OutpatientReceptionEntity reception = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("Inpatient reception not found. receptionId=" + receptionId));
        InpatientReceptionEntity inpatient = inpatientRepository.findByReceptionId(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("Inpatient detail not found. receptionId=" + receptionId));
        InpatientBedAssignmentEntity bedAssignment = inpatientBedAssignmentRepository
                .findTopByInpatientAdmissionIdOrderByAssignmentDatetimeDesc(inpatient.getInpatientAdmissionId())
                .orElse(null);

        enrichDisplayNames(reception);
        reception.setStatus(normalizeStatus(reception.getStatus()));
        return toInpatientDto(reception, inpatient, bedAssignment);
    }

    @Override
    @Transactional
    public void createInpatientReception(InpatientReceptionDTO request) {
        String receptionNo = resolveCreateReceptionNo(request);
        if (request.getPatientId() == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        if (request.getDepartmentId() == null) {
            throw new IllegalArgumentException("departmentId is required");
        }
        if (request.getAdmissionPlanAt() == null) {
            throw new IllegalArgumentException("admissionPlanAt is required");
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
        reception.setVisitType("INPATIENT");
        reception.setDepartmentId(request.getDepartmentId());
        reception.setDepartmentName(resolveDepartmentName(request.getDepartmentId(), request.getDepartmentName()));
        reception.setDoctorId(request.getDoctorId());
        reception.setDoctorName(resolveDoctorName(request.getDoctorId(), request.getDepartmentId(), request.getDoctorName()));
        reception.setReservationId(request.getReservationId());
        reception.setScheduledAt(request.getScheduledAt());
        reception.setArrivedAt(request.getArrivedAt());
        reception.setStatus(normalizedStatus);
        reception.setNote(request.getNote());
        reception.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        applyStatusSideEffects(reception, normalizedStatus, reasonCode, reasonText);

        OutpatientReceptionEntity savedReception = receptionRepository.save(reception);

        InpatientReceptionEntity inpatient = new InpatientReceptionEntity();
        inpatient.setReceptionId(savedReception.getReceptionId());
        inpatient.setPatientId(request.getPatientId());
        inpatient.setAdmissionStatusCd(normalizedStatus);
        inpatient.setAdmissionReason(request.getNote());
        inpatient.setDepartmentId(request.getDepartmentId());
        inpatient.setDoctorId(request.getDoctorId());
        inpatient.setActiveYn(toYn(savedReception.getIsActive()));
        inpatient.setAdmissionPlanAt(request.getAdmissionPlanAt());
        InpatientReceptionEntity savedInpatient = inpatientRepository.save(inpatient);

        upsertAdmissionDecision(savedInpatient, request);

        InpatientBedAssignmentEntity savedBedAssignment = upsertBedAssignment(savedInpatient.getInpatientAdmissionId(), null, request);
        if (savedBedAssignment != null) {
            saveBedAssignmentHistory(
                    savedBedAssignment.getBedAssignmentId(),
                    null,
                    savedBedAssignment.getBedId(),
                    request.getDoctorId(),
                    request.getNote()
            );
        }

        saveAdmissionAudit(
                savedInpatient.getInpatientAdmissionId(),
                "CREATE",
                null,
                toAdmissionAuditValue(savedInpatient, savedBedAssignment),
                request.getNote(),
                request.getDoctorId()
        );
    }

    private String resolveCreateReceptionNo(InpatientReceptionDTO request) {
        String requestedReceptionNo = trimToNull(request.getReceptionNo());
        if (requestedReceptionNo != null) {
            if (receptionRepository.existsByReceptionNo(requestedReceptionNo)) {
                throw new IllegalArgumentException("Duplicated receptionNo: " + requestedReceptionNo);
            }
            return requestedReceptionNo;
        }

        String generatedReceptionNo = receptionNumberSequenceClient.nextReceptionNo("INPATIENT");
        if (receptionRepository.existsByReceptionNo(generatedReceptionNo)) {
            throw new IllegalStateException("Generated duplicated receptionNo: " + generatedReceptionNo);
        }
        return generatedReceptionNo;
    }

    @Override
    @Transactional
    public void updateInpatientReception(Long receptionId, InpatientReceptionDTO request) {
        OutpatientReceptionEntity reception = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("Inpatient reception not found. receptionId=" + receptionId));
        InpatientReceptionEntity inpatient = inpatientRepository.findByReceptionId(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("Inpatient detail not found. receptionId=" + receptionId));
        InpatientBedAssignmentEntity bedAssignment = inpatientBedAssignmentRepository
                .findTopByInpatientAdmissionIdOrderByAssignmentDatetimeDesc(inpatient.getInpatientAdmissionId())
                .orElse(null);

        String beforeAuditValue = toAdmissionAuditValue(inpatient, bedAssignment);
        Long beforeBedId = bedAssignment != null ? bedAssignment.getBedId() : null;

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
            reception.setDoctorName(resolveDoctorName(
                    reception.getDoctorId(),
                    reception.getDepartmentId(),
                    firstNonBlank(request.getDoctorName(), reception.getDoctorName())
            ));
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

        if (request.getPatientId() != null) {
            inpatient.setPatientId(request.getPatientId());
        }
        inpatient.setAdmissionStatusCd(targetStatus);
        if (request.getAdmissionPlanAt() != null) {
            inpatient.setAdmissionPlanAt(request.getAdmissionPlanAt());
        }
        if (request.getDepartmentId() != null) {
            inpatient.setDepartmentId(request.getDepartmentId());
        }
        if (request.getDoctorId() != null) {
            inpatient.setDoctorId(request.getDoctorId());
        }
        if (request.getNote() != null) {
            inpatient.setAdmissionReason(request.getNote());
        }
        inpatient.setActiveYn(toYn(reception.getIsActive()));

        receptionRepository.save(reception);
        InpatientReceptionEntity savedInpatient = inpatientRepository.save(inpatient);
        upsertAdmissionDecision(savedInpatient, request);

        InpatientBedAssignmentEntity savedBedAssignment = upsertBedAssignment(inpatient.getInpatientAdmissionId(), bedAssignment, request);
        if (savedBedAssignment != null && (request.getWardId() != null || request.getRoomId() != null)) {
            saveBedAssignmentHistory(
                    savedBedAssignment.getBedAssignmentId(),
                    beforeBedId,
                    savedBedAssignment.getBedId(),
                    request.getDoctorId(),
                    request.getNote()
            );
        }

        saveAdmissionAudit(
                savedInpatient.getInpatientAdmissionId(),
                "UPDATE",
                beforeAuditValue,
                toAdmissionAuditValue(savedInpatient, savedBedAssignment),
                request.getNote(),
                request.getDoctorId()
        );
    }

    private void upsertAdmissionDecision(InpatientReceptionEntity inpatient, InpatientReceptionDTO request) {
        InpatientAdmissionDecisionEntity decision = inpatientAdmissionDecisionRepository
                .findTopByInpatientAdmissionIdOrderByDecisionDatetimeDesc(inpatient.getInpatientAdmissionId())
                .orElseGet(InpatientAdmissionDecisionEntity::new);

        if (decision.getDecisionId() == null) {
            decision.setInpatientAdmissionId(inpatient.getInpatientAdmissionId());
        }
        decision.setDecisionDatetime(request.getAdmissionPlanAt() != null ? request.getAdmissionPlanAt() : LocalDateTime.now());
        decision.setDecisionDoctorId(request.getDoctorId());
        decision.setDecisionReason(request.getNote());
        decision.setDecisionNote(request.getNote());

        inpatientAdmissionDecisionRepository.save(decision);
    }

    private InpatientBedAssignmentEntity upsertBedAssignment(
            Long inpatientAdmissionId,
            InpatientBedAssignmentEntity existing,
            InpatientReceptionDTO request
    ) {
        if (request.getWardId() == null && request.getRoomId() == null && existing == null) {
            return null;
        }

        InpatientBedAssignmentEntity bedAssignment = existing != null ? existing : new InpatientBedAssignmentEntity();
        if (bedAssignment.getBedAssignmentId() == null) {
            bedAssignment.setInpatientAdmissionId(inpatientAdmissionId);
            bedAssignment.setAssignmentStatusCd("ASSIGNED");
        }
        if (request.getWardId() != null) {
            bedAssignment.setWardId(request.getWardId());
        }
        if (request.getRoomId() != null) {
            bedAssignment.setRoomId(request.getRoomId());
            bedAssignment.setBedId(request.getRoomId());
        }
        bedAssignment.setAssignmentDatetime(request.getAdmissionPlanAt() != null ? request.getAdmissionPlanAt() : LocalDateTime.now());
        if (request.getDoctorId() != null) {
            bedAssignment.setAssignedBy(request.getDoctorId());
        }
        if (request.getNote() != null) {
            bedAssignment.setRemark(request.getNote());
        }

        return inpatientBedAssignmentRepository.save(bedAssignment);
    }

    private void saveBedAssignmentHistory(
            Long bedAssignmentId,
            Long beforeBedId,
            Long afterBedId,
            String changedBy,
            String changeReason
    ) {
        InpatientBedAssignmentHistoryEntity history = new InpatientBedAssignmentHistoryEntity();
        history.setBedAssignmentId(bedAssignmentId);
        history.setBeforeBedId(beforeBedId);
        history.setAfterBedId(afterBedId);
        history.setChangeReason(changeReason);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        inpatientBedAssignmentHistoryRepository.save(history);
    }

    private void saveAdmissionAudit(
            Long inpatientAdmissionId,
            String changeField,
            String beforeValue,
            String afterValue,
            String changeReason,
            String changedBy
    ) {
        InpatientAdmissionAuditEntity audit = new InpatientAdmissionAuditEntity();
        audit.setInpatientAdmissionId(inpatientAdmissionId);
        audit.setChangeFieldNm(changeField);
        audit.setBeforeValue(beforeValue);
        audit.setAfterValue(afterValue);
        audit.setChangeReason(changeReason);
        audit.setChangedBy(changedBy);
        inpatientAdmissionAuditRepository.save(audit);
    }

    private String toAdmissionAuditValue(InpatientReceptionEntity admission, InpatientBedAssignmentEntity bedAssignment) {
        Long wardId = bedAssignment != null ? bedAssignment.getWardId() : null;
        Long roomId = bedAssignment != null ? bedAssignment.getRoomId() : null;
        Long bedId = bedAssignment != null ? bedAssignment.getBedId() : null;
        return "status=" + admission.getAdmissionStatusCd()
                + ",deptId=" + admission.getDepartmentId()
                + ",doctorId=" + admission.getDoctorId()
                + ",wardId=" + wardId
                + ",roomId=" + roomId
                + ",bedId=" + bedId;
    }

    private InpatientReceptionDTO toInpatientDto(
            OutpatientReceptionEntity reception,
            InpatientReceptionEntity inpatient,
            InpatientBedAssignmentEntity bedAssignment
    ) {
        InpatientReceptionDTO dto = new InpatientReceptionDTO();
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
        dto.setAdmissionPlanAt(inpatient.getAdmissionPlanAt());
        dto.setWardId(bedAssignment != null ? bedAssignment.getWardId() : null);
        dto.setRoomId(bedAssignment != null ? bedAssignment.getRoomId() : null);
        return dto;
    }

    private InpatientReceptionDTO normalizeStatusForResponse(InpatientReceptionDTO dto) {
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
        rules.put("COMPLETED", Collections.emptySet());
        rules.put("CANCELLED", Collections.emptySet());
        rules.put("INACTIVE", Collections.emptySet());
        return rules;
    }

    private String resolveReasonCodeForStatus(String status, InpatientReceptionDTO request, OutpatientReceptionEntity existing) {
        if ("CANCELLED".equals(status)) {
            return firstNonBlank(
                    trimToNull(request == null ? null : request.getNote()) != null ? "CANCELLED_BY_USER" : null,
                    existing == null ? null : existing.getCancelReasonCode(),
                    existing == null ? null : existing.getInactiveReasonCode()
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

    private String resolveReasonTextForStatus(String status, InpatientReceptionDTO request, OutpatientReceptionEntity existing) {
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
            reception.setDoctorName(resolveDoctorName(
                    reception.getDoctorId(),
                    reception.getDepartmentId(),
                    reception.getDoctorName()
            ));
        } catch (RuntimeException ignored) {
            // keep current value
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

    private String toYn(Boolean value) {
        if (value == null) {
            return "Y";
        }
        return value ? "Y" : "N";
    }
}
