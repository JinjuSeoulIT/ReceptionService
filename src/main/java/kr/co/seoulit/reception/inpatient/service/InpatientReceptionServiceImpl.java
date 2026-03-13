package kr.co.seoulit.reception.inpatient.service;

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
import kr.co.seoulit.reception.outpatient.entity.OutpatientReceptionEntity;
import kr.co.seoulit.reception.outpatient.repository.OutpatientReceptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InpatientReceptionServiceImpl implements InpatientReceptionService {

    private final OutpatientReceptionRepository receptionRepository;
    private final InpatientReceptionRepository inpatientRepository;
    private final InpatientBedAssignmentRepository inpatientBedAssignmentRepository;
    private final InpatientAdmissionDecisionRepository inpatientAdmissionDecisionRepository;
    private final InpatientBedAssignmentHistoryRepository inpatientBedAssignmentHistoryRepository;
    private final InpatientAdmissionAuditRepository inpatientAdmissionAuditRepository;
    private final InpatientReceptionMapper inpatientMyBatisMapper;

    @Override
    public List<InpatientReceptionDTO> getInpatientReceptionList(Map<String, Object> searchCondition) {
        String searchType = (String) searchCondition.get("searchType");
        String searchValue = (String) searchCondition.get("searchValue");
        return inpatientMyBatisMapper.selectInpatientReceptions(searchType, searchValue);
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

        return toInpatientDto(reception, inpatient, bedAssignment);
    }

    @Override
    @Transactional
    public void createInpatientReception(InpatientReceptionDTO request) {
        if (request.getReceptionNo() == null || request.getReceptionNo().isBlank()) {
            throw new IllegalArgumentException("receptionNo is required");
        }
        if (request.getPatientId() == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        if (request.getDepartmentId() == null) {
            throw new IllegalArgumentException("departmentId is required");
        }
        if (request.getAdmissionPlanAt() == null) {
            throw new IllegalArgumentException("admissionPlanAt is required");
        }

        OutpatientReceptionEntity reception = new OutpatientReceptionEntity();
        reception.setReceptionNo(request.getReceptionNo());
        reception.setPatientId(request.getPatientId());
        reception.setPatientName(resolvePatientNameWithFallback(request.getPatientId(), request.getPatientName()));
        reception.setVisitType("INPATIENT");
        reception.setDepartmentId(request.getDepartmentId());
        reception.setDepartmentName(resolveDepartmentNameWithFallback(request.getDepartmentId(), request.getDepartmentName()));
        reception.setDoctorId(request.getDoctorId());
        reception.setDoctorName(resolveDoctorNameWithFallback(request.getDoctorId(), request.getDoctorName()));
        reception.setReservationId(request.getReservationId());
        reception.setScheduledAt(request.getScheduledAt());
        reception.setArrivedAt(request.getArrivedAt());
        reception.setStatus(request.getStatus() != null ? request.getStatus() : "WAITING");
        reception.setNote(request.getNote());
        reception.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        OutpatientReceptionEntity savedReception = receptionRepository.save(reception);

        InpatientReceptionEntity inpatient = new InpatientReceptionEntity();
        inpatient.setReceptionId(savedReception.getReceptionId());
        inpatient.setPatientId(request.getPatientId());
        inpatient.setAdmissionStatusCd(request.getStatus() != null ? request.getStatus() : "WAITING");
        inpatient.setAdmissionReason(request.getNote());
        inpatient.setDepartmentId(request.getDepartmentId());
        inpatient.setDoctorId(request.getDoctorId());
        inpatient.setActiveYn(toYn(request.getIsActive()));
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

        if (request.getReceptionNo() != null && !request.getReceptionNo().isBlank()) {
            reception.setReceptionNo(request.getReceptionNo());
        }
        if (request.getPatientId() != null) {
            reception.setPatientId(request.getPatientId());
        }
        if (request.getPatientName() != null) {
            reception.setPatientName(request.getPatientName());
        } else if (request.getPatientId() != null) {
            reception.setPatientName(resolvePatientNameWithFallback(request.getPatientId(), null));
        }
        if (request.getDepartmentId() != null) {
            reception.setDepartmentId(request.getDepartmentId());
        }
        if (request.getDepartmentName() != null) {
            reception.setDepartmentName(request.getDepartmentName());
        } else if (request.getDepartmentId() != null) {
            reception.setDepartmentName(resolveDepartmentNameWithFallback(request.getDepartmentId(), null));
        }
        if (request.getDoctorId() != null) {
            reception.setDoctorId(request.getDoctorId());
        }
        if (request.getDoctorName() != null) {
            reception.setDoctorName(request.getDoctorName());
        } else if (request.getDoctorId() != null) {
            reception.setDoctorName(resolveDoctorNameWithFallback(request.getDoctorId(), null));
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
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            reception.setStatus(request.getStatus());
        }
        if (request.getNote() != null) {
            reception.setNote(request.getNote());
        }
        if (request.getIsActive() != null) {
            reception.setIsActive(request.getIsActive());
        }

        if (request.getPatientId() != null) {
            inpatient.setPatientId(request.getPatientId());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            inpatient.setAdmissionStatusCd(request.getStatus());
        }
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
        if (request.getIsActive() != null) {
            inpatient.setActiveYn(toYn(request.getIsActive()));
        }

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
            Long changedBy,
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
            Long changedBy
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
        dto.setStatus(reception.getStatus());
        dto.setNote(reception.getNote());
        dto.setIsActive(reception.getIsActive());
        dto.setCreatedAt(reception.getCreatedAt());
        dto.setUpdatedAt(reception.getUpdatedAt());
        dto.setAdmissionPlanAt(inpatient.getAdmissionPlanAt());
        dto.setWardId(bedAssignment != null ? bedAssignment.getWardId() : null);
        dto.setRoomId(bedAssignment != null ? bedAssignment.getRoomId() : null);
        return dto;
    }

    private String resolvePatientNameWithFallback(Long patientId, String fallback) {
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        if (patientId == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        return "PATIENT-" + patientId;
    }

    private String resolveDepartmentNameWithFallback(Long departmentId, String fallback) {
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        if (departmentId == null) {
            throw new IllegalArgumentException("departmentId is required");
        }
        return "DEPT-" + departmentId;
    }

    private String resolveDoctorNameWithFallback(Long doctorId, String fallback) {
        if (doctorId == null) {
            return null;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return "DOCTOR-" + doctorId;
    }

    private String toYn(Boolean value) {
        if (value == null) {
            return "Y";
        }
        return value ? "Y" : "N";
    }
}
