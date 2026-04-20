package kr.co.seoulit.reception.outpatient.service;

import kr.co.seoulit.common.audit.AuditLogService;
import kr.co.seoulit.common.client.PatientServiceClient;
import kr.co.seoulit.common.sequence.ReceptionNumberSequenceClient;
import kr.co.seoulit.reception.exception.ReceptionNotFoundException;
import kr.co.seoulit.reception.outpatient.mapstruct.ReceptionReqMapStruct;
import kr.co.seoulit.reception.outpatient.mapstruct.ReceptionResMapStruct;
import kr.co.seoulit.reception.outpatient.dto.OutpatientCallHistoryDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientClosureReasonDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientQualificationItemDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientQualificationSnapshotDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionAuditDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusChangedEventDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusHistoryDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientSettlementSnapshotDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientVisitClosureDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientVisitClosureHistoryDTO;
import kr.co.seoulit.reception.outpatient.entity.OutpatientReceptionDetailEntity;
import kr.co.seoulit.reception.outpatient.entity.OutpatientReceptionEntity;
import kr.co.seoulit.reception.outpatient.entity.OutpatientReceptionStatusHistoryEntity;
import kr.co.seoulit.reception.outpatient.entity.OutpatientWaitingQueueEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionAuditEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionCallHistoryEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionClosureReasonEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionQualificationItemEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionQualificationSnapshotEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionSettlementSnapshotEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionVisitClosureEntity;
import kr.co.seoulit.reception.outpatient.entity.ReceptionVisitClosureHistoryEntity;
import kr.co.seoulit.reception.outpatient.mapper.OutpatientReceptionMapper;
import kr.co.seoulit.reception.outpatient.realtime.OutpatientReceptionStatusEventPublisher;
import kr.co.seoulit.reception.outpatient.repository.OutpatientReceptionDetailRepository;
import kr.co.seoulit.reception.outpatient.repository.OutpatientReceptionRepository;
import kr.co.seoulit.reception.outpatient.repository.OutpatientReceptionStatusHistoryRepository;
import kr.co.seoulit.reception.outpatient.repository.OutpatientWaitingQueueRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionAuditRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionCallHistoryRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionClosureReasonRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionQualificationItemRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionQualificationSnapshotRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionSettlementSnapshotRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionVisitClosureHistoryRepository;
import kr.co.seoulit.reception.outpatient.repository.ReceptionVisitClosureRepository;
import kr.co.seoulit.reception.reservation.entity.ReservationToReceptionHistoryEntity;
import kr.co.seoulit.reception.reservation.repository.ReservationToReceptionHistoryRepository;
import kr.co.seoulit.reception.service.DepartmentService;
import kr.co.seoulit.reception.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutpatientReceptionServiceImpl implements OutpatientReceptionService {

    private static final Set<String> REASON_REQUIRED_STATUSES = Set.of("CANCELLED", "INACTIVE", "HOLD");
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = createStatusTransitionRules();
    private final OutpatientReceptionRepository receptionRepository;
    private final OutpatientReceptionMapper receptionMyBatisMapper;
    private final ReceptionResMapStruct receptionResMapStruct;
    private final ReceptionReqMapStruct receptionReqMapStruct;
    private final OutpatientReceptionStatusHistoryRepository receptionStatusHistoryRepository;
    private final OutpatientReceptionDetailRepository receptionDetailRepository;
    private final OutpatientWaitingQueueRepository waitingQueueRepository;
    private final ReceptionQualificationSnapshotRepository qualificationSnapshotRepository;
    private final ReceptionQualificationItemRepository qualificationItemRepository;
    private final ReceptionCallHistoryRepository callHistoryRepository;
    private final ReceptionVisitClosureRepository visitClosureRepository;
    private final ReceptionClosureReasonRepository closureReasonRepository;
    private final ReceptionVisitClosureHistoryRepository visitClosureHistoryRepository;
    private final ReceptionSettlementSnapshotRepository settlementSnapshotRepository;
    private final ReceptionAuditRepository receptionAuditRepository;
    private final ReservationToReceptionHistoryRepository reservationToReceptionHistoryRepository;
    private final PatientServiceClient patientServiceClient;
    private final ReceptionNumberSequenceClient receptionNumberSequenceClient;
    private final AuditLogService auditLogService;
    private final OutpatientReceptionStatusEventPublisher receptionStatusEventPublisher;
    private final DepartmentService departmentService;
    private final DoctorService doctorService;

    @Override
    @Cacheable(value = "RECEPTION_LIST")
    public List<OutpatientReceptionDTO> getReceptionList(Map<String, Object> searchCondition) {
        String searchType = (String) searchCondition.get("searchType");
        String searchValue = (String) searchCondition.get("searchValue");
        String dateFrom = (String) searchCondition.get("dateFrom");
        String dateTo = (String) searchCondition.get("dateTo");
        String departmentId = trimToNull((String) searchCondition.get("departmentId"));
        String doctorId = trimToNull((String) searchCondition.get("doctorId"));

        return receptionMyBatisMapper.selectReceptions(
                searchType,
                searchValue,
                dateFrom,
                dateTo,
                departmentId,
                doctorId
        ).stream().map(this::normalizeStatusForResponse).collect(Collectors.toList());
    }

    @Override
    @Cacheable(key = "#receptionId", value = "RECEPTION")
    public OutpatientReceptionDTO getReception(Long receptionId) {
        OutpatientReceptionEntity entity = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new ReceptionNotFoundException("Reception not found. receptionId=" + receptionId));
        enrichDisplayNames(entity);
        entity.setStatus(normalizeStatus(entity.getStatus()));
        return normalizeStatusForResponse(receptionResMapStruct.toDto(entity));
    }

    @Override
    public List<OutpatientReceptionDTO> getReceptionQueue(String departmentId, String doctorId, String date) {
        return receptionMyBatisMapper.selectQueue(departmentId, doctorId, date)
                .stream()
                .map(this::normalizeStatusForResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "RECEPTION_LIST", allEntries = true)
    public void createReception(OutpatientReceptionDTO reception) {
        String receptionNo = resolveCreateReceptionNo(reception);
        if (reception.getDepartmentId() == null) {
            throw new IllegalArgumentException("departmentId is required");
        }
        if (reception.getReservationId() != null
                && receptionRepository.existsByReservationId(reception.getReservationId())) {
            throw new IllegalArgumentException(
                    "Duplicated reservationId: " + reception.getReservationId()
            );
        }

        String normalizedStatus = normalizeStatus(defaultIfBlank(reception.getStatus(), "WAITING"));
        assertStatusSupported(normalizedStatus);
        String reasonCode = resolveReasonCodeForStatus(normalizedStatus, reception, null);
        String reasonText = resolveReasonTextForStatus(normalizedStatus, reception, null);
        validateReasonIfRequired(normalizedStatus, reasonCode, reasonText);

        OutpatientReceptionEntity entity = receptionReqMapStruct.toEntity(reception);
        entity.setReceptionNo(receptionNo);
        PatientRef resolvedPatient = resolvePatient(entity.getPatientId(), entity.getPatientName());
        entity.setPatientId(resolvedPatient.patientId());
        entity.setPatientName(resolvedPatient.patientName());
        entity.setDepartmentName(resolveDepartmentName(entity.getDepartmentId(), entity.getDepartmentName()));
        doctorService.validateActiveDoctor(entity.getDoctorId(), entity.getDepartmentId());
        entity.setDoctorName(resolveDoctorName(entity.getDoctorId(), entity.getDepartmentId(), entity.getDoctorName()));
        entity.setStatus(normalizedStatus);
        entity.setVisitType(defaultIfBlank(entity.getVisitType(), "OUTPATIENT"));
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }
        applyStatusSideEffects(entity, normalizedStatus, reasonCode, reasonText);

        OutpatientReceptionEntity saved = receptionRepository.save(entity);
        upsertOutpatientDetail(saved);
        OutpatientWaitingQueueEntity queue = upsertWaitingQueue(saved);
        syncQualificationSnapshot(saved);
        syncVisitClosure(saved, saved.getStatus(), reception.getCreatedBy(), reasonCode, reasonText);
        snapshotSettlement(saved);
        saveReceptionAudit(
                saved.getReceptionId(),
                "CREATE",
                "*",
                null,
                toAuditValue(saved),
                reasonText,
                reception.getCreatedBy()
        );
        saveReservationConversionHistory(saved, reception.getCreatedBy(), "SUCCESS", "Reception created");
        auditLogService.log(
                "RECEPTION",
                saved.getReceptionId(),
                "CREATE",
                reception.getCreatedBy(),
                reasonCode,
                reasonText,
                null,
                receptionResMapStruct.toDto(saved)
        );
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "RECEPTION", key = "#receptionId"),
            @CacheEvict(value = "RECEPTION_LIST", allEntries = true)
    })
    public void updateReception(Long receptionId, OutpatientReceptionDTO reception) {
        OutpatientReceptionEntity existing = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new ReceptionNotFoundException("Reception not found. receptionId=" + receptionId));

        OutpatientReceptionDTO before = receptionResMapStruct.toDto(existing);
        String beforeAuditValue = toAuditValue(existing);
        Long beforeReservationId = existing.getReservationId();

        if (trimToNull(reception.getReceptionNo()) != null) {
            String nextReceptionNo = trimToNull(reception.getReceptionNo());
            if (!Objects.equals(existing.getReceptionNo(), nextReceptionNo)) {
                receptionRepository.findByReceptionNo(nextReceptionNo)
                        .filter(item -> !Objects.equals(item.getReceptionId(), existing.getReceptionId()))
                        .ifPresent(item -> {
                            throw new IllegalArgumentException("Duplicated receptionNo: " + nextReceptionNo);
                        });
                existing.setReceptionNo(nextReceptionNo);
            }
        }
        if (reception.getPatientId() != null) {
            existing.setPatientId(reception.getPatientId());
        }
        if (trimToNull(reception.getPatientName()) != null || reception.getPatientId() != null) {
            PatientRef resolvedPatient = resolvePatient(
                    existing.getPatientId(),
                    firstNonBlank(reception.getPatientName(), existing.getPatientName())
            );
            existing.setPatientId(resolvedPatient.patientId());
            existing.setPatientName(resolvedPatient.patientName());
        }
        if (reception.getVisitType() != null && !reception.getVisitType().isBlank()) {
            existing.setVisitType(reception.getVisitType());
        }
        if (reception.getDepartmentId() != null) {
            existing.setDepartmentId(reception.getDepartmentId());
        }
        if (trimToNull(reception.getDepartmentName()) != null || reception.getDepartmentId() != null) {
            existing.setDepartmentName(resolveDepartmentName(existing.getDepartmentId(), firstNonBlank(reception.getDepartmentName(), existing.getDepartmentName())));
        }
        if (reception.getDoctorId() != null) {
            existing.setDoctorId(reception.getDoctorId());
        }
        doctorService.validateActiveDoctor(existing.getDoctorId(), existing.getDepartmentId());
        if (trimToNull(reception.getDoctorName()) != null || reception.getDoctorId() != null) {
            existing.setDoctorName(resolveDoctorName(existing.getDoctorId(), existing.getDepartmentId(), firstNonBlank(reception.getDoctorName(), existing.getDoctorName())));
        }
        if (reception.getReservationId() != null) {
            existing.setReservationId(reception.getReservationId());
        }
        if (reception.getScheduledAt() != null) {
            existing.setScheduledAt(reception.getScheduledAt());
        }
        if (reception.getArrivedAt() != null) {
            existing.setArrivedAt(reception.getArrivedAt());
        }
        if (reception.getNote() != null) {
            existing.setNote(reception.getNote());
        }
        if (reception.getCreatedBy() != null) {
            existing.setCreatedBy(reception.getCreatedBy());
        }
        if (reception.getUpdatedBy() != null) {
            existing.setUpdatedBy(reception.getUpdatedBy());
        }

        String fromStatus = normalizeStatus(existing.getStatus());
        String targetStatus = normalizeStatus(defaultIfBlank(reception.getStatus(), existing.getStatus()));
        validateStatusTransition(fromStatus, targetStatus);

        String reasonCode = resolveReasonCodeForStatus(targetStatus, reception, existing);
        String reasonText = resolveReasonTextForStatus(targetStatus, reception, existing);
        validateReasonIfRequired(targetStatus, reasonCode, reasonText);

        existing.setStatus(targetStatus);
        applyStatusSideEffects(existing, targetStatus, reasonCode, reasonText);

        OutpatientReceptionEntity saved = receptionRepository.save(existing);
        enrichDisplayNames(saved);
        upsertOutpatientDetail(saved);
        OutpatientWaitingQueueEntity queue = upsertWaitingQueue(saved);
        syncQualificationSnapshot(saved);
        syncVisitClosure(
                saved,
                targetStatus,
                reception.getUpdatedBy(),
                reasonCode,
                reasonText
        );
        snapshotSettlement(saved);
        saveReceptionAudit(
                saved.getReceptionId(),
                "UPDATE",
                "*",
                beforeAuditValue,
                toAuditValue(saved),
                reasonText,
                reception.getUpdatedBy()
        );

        if (!Objects.equals(fromStatus, targetStatus)) {
            saveStatusHistory(saved.getReceptionId(), fromStatus, targetStatus, reception.getUpdatedBy(), reasonCode, reasonText);
            publishStatusChangedAfterCommit(saved, fromStatus, targetStatus);
        }

        if (beforeReservationId == null && saved.getReservationId() != null) {
            saveReservationConversionHistory(saved, reception.getUpdatedBy(), "SUCCESS", "Reservation linked");
        }
        auditLogService.log(
                "RECEPTION",
                saved.getReceptionId(),
                "UPDATE",
                reception.getUpdatedBy(),
                reasonCode,
                reasonText,
                before,
                receptionResMapStruct.toDto(saved)
        );
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "RECEPTION", key = "#receptionId"),
            @CacheEvict(value = "RECEPTION_LIST", allEntries = true)
    })
    public OutpatientReceptionDTO updateReceptionStatus(Long receptionId, String status, Long changedBy, String reasonCode, String reasonText) {
        return doUpdateReceptionStatus(receptionId, status, changedBy, reasonCode, reasonText);
    }

    @Override
    public List<OutpatientReceptionStatusHistoryDTO> getReceptionStatusHistory(Long receptionId) {
        return receptionStatusHistoryRepository.findByReceptionIdOrderByChangedAtAsc(receptionId)
                .stream()
                .map(this::toHistoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OutpatientQualificationSnapshotDTO> getQualificationSnapshots(Long receptionId) {
        return qualificationSnapshotRepository.findByReceptionIdOrderBySnapshotDatetimeDesc(receptionId)
                .stream()
                .map(this::toQualificationSnapshotDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OutpatientQualificationItemDTO> getLatestQualificationItems(Long receptionId) {
        return qualificationSnapshotRepository.findTopByReceptionIdOrderBySnapshotDatetimeDesc(receptionId)
                .map(snapshot -> qualificationItemRepository.findByQualificationSnapshotIdOrderByDisplayOrderAsc(snapshot.getQualificationSnapshotId()))
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toQualificationItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OutpatientCallHistoryDTO> getCallHistory(Long receptionId) {
        return waitingQueueRepository.findByReceptionId(receptionId)
                .map(queue -> callHistoryRepository.findByWaitingQueueIdOrderByCallDatetimeDesc(queue.getWaitingQueueId()))
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toCallHistoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public OutpatientVisitClosureDTO getVisitClosure(Long receptionId) {
        return visitClosureRepository.findByReceptionId(receptionId)
                .map(this::toVisitClosureDto)
                .orElse(null);
    }

    @Override
    public List<OutpatientClosureReasonDTO> getClosureReasons() {
        return closureReasonRepository.findByUsableYnOrderBySortOrderAsc("Y")
                .stream()
                .map(this::toClosureReasonDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OutpatientVisitClosureHistoryDTO> getVisitClosureHistory(Long receptionId) {
        return visitClosureRepository.findByReceptionId(receptionId)
                .map(closure -> visitClosureHistoryRepository.findByVisitClosureIdOrderByChangedAtDesc(closure.getVisitClosureId()))
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toVisitClosureHistoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OutpatientSettlementSnapshotDTO> getSettlementSnapshots(Long receptionId) {
        return settlementSnapshotRepository.findByReceptionIdOrderBySnapshotDatetimeDesc(receptionId)
                .stream()
                .map(this::toSettlementSnapshotDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OutpatientReceptionAuditDTO> getReceptionAudits(Long receptionId) {
        return receptionAuditRepository.findByReceptionIdOrderByChangedAtDesc(receptionId)
                .stream()
                .map(this::toReceptionAuditDto)
                .collect(Collectors.toList());
    }

    private OutpatientReceptionDTO doUpdateReceptionStatus(
            Long receptionId,
            String status,
            Long changedBy,
            String reasonCode,
            String reasonText
    ) {
        OutpatientReceptionEntity existing = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new ReceptionNotFoundException("Reception not found. receptionId=" + receptionId));

        OutpatientReceptionDTO before = receptionResMapStruct.toDto(existing);
        String beforeAuditValue = toAuditValue(existing);
        String fromStatus = normalizeStatus(existing.getStatus());
        String targetStatus = normalizeStatus(status);
        validateStatusTransition(fromStatus, targetStatus);

        String normalizedReasonCode = trimToNull(reasonCode);
        String normalizedReasonText = trimToNull(reasonText);
        if (REASON_REQUIRED_STATUSES.contains(targetStatus)) {
            normalizedReasonCode = firstNonBlank(normalizedReasonCode, resolveReasonCodeForStatus(targetStatus, null, existing));
            normalizedReasonText = firstNonBlank(normalizedReasonText, resolveReasonTextForStatus(targetStatus, null, existing));
        }
        validateReasonIfRequired(targetStatus, normalizedReasonCode, normalizedReasonText);

        existing.setStatus(targetStatus);
        existing.setUpdatedBy(changedBy);
        applyStatusSideEffects(existing, targetStatus, normalizedReasonCode, normalizedReasonText);

        OutpatientReceptionEntity saved = receptionRepository.save(existing);
        upsertOutpatientDetail(saved);
        OutpatientWaitingQueueEntity queue = upsertWaitingQueue(saved);
        syncQualificationSnapshot(saved);
        syncVisitClosure(saved, targetStatus, changedBy, normalizedReasonCode, normalizedReasonText);
        snapshotSettlement(saved);
        saveStatusHistory(receptionId, fromStatus, targetStatus, changedBy, normalizedReasonCode, normalizedReasonText);
        publishStatusChangedAfterCommit(saved, fromStatus, targetStatus);

        saveReceptionAudit(
                saved.getReceptionId(),
                "STATUS_CHANGE",
                "STATUS",
                beforeAuditValue,
                toAuditValue(saved),
                normalizedReasonText,
                changedBy
        );

        auditLogService.log(
                "RECEPTION",
                saved.getReceptionId(),
                "STATUS_CHANGE",
                changedBy,
                normalizedReasonCode,
                normalizedReasonText,
                before,
                receptionResMapStruct.toDto(saved)
        );

        return normalizeStatusForResponse(receptionResMapStruct.toDto(saved));
    }

    private void enrichDisplayNames(OutpatientReceptionEntity reception) {
        if (reception == null) {
            return;
        }

        if (reception.getPatientId() != null) {
            try {
                PatientRef resolved = resolvePatient(reception.getPatientId(), reception.getPatientName());
                reception.setPatientId(resolved.patientId());
                reception.setPatientName(resolved.patientName());
            } catch (RuntimeException ignored) {
                if (trimToNull(reception.getPatientName()) == null) {
                    reception.setPatientName("PATIENT-" + reception.getPatientId());
                }
            }
        }

        try {
            reception.setDepartmentName(
                    resolveDepartmentName(reception.getDepartmentId(), reception.getDepartmentName())
            );
        } catch (RuntimeException ignored) {
            // keep current value when department metadata cannot be resolved
        }

        try {
            reception.setDoctorName(
                    resolveDoctorName(reception.getDoctorId(), reception.getDepartmentId(), reception.getDoctorName())
            );
        } catch (RuntimeException ignored) {
            // keep current value when doctor metadata cannot be resolved
        }
    }

    private void saveStatusHistory(
            Long receptionId,
            String fromStatus,
            String toStatus,
            Long changedBy,
            String reasonCode,
            String reasonText
    ) {
        OutpatientReceptionStatusHistoryEntity history = new OutpatientReceptionStatusHistoryEntity();
        history.setReceptionId(receptionId);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedBy(changedBy);
        history.setReasonCode(reasonCode);
        history.setReasonText(reasonText);
        receptionStatusHistoryRepository.save(history);
    }

    private void publishStatusChangedAfterCommit(
            OutpatientReceptionEntity reception,
            String fromStatus,
            String toStatus
    ) {
        if (Objects.equals(fromStatus, toStatus)) {
            return;
        }
        // reception entity keeps display fields as transient, so make sure
        // patient/department/doctor names are resolved before sending SSE.
        enrichDisplayNames(reception);
        OutpatientReceptionStatusChangedEventDTO event = buildStatusChangedEvent(
                reception,
                fromStatus,
                toStatus
        );
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    receptionStatusEventPublisher.publishStatusChanged(event);
                }
            });
            return;
        }
        receptionStatusEventPublisher.publishStatusChanged(event);
    }

    private OutpatientReceptionStatusChangedEventDTO buildStatusChangedEvent(
            OutpatientReceptionEntity reception,
            String fromStatus,
            String toStatus
    ) {
        OutpatientReceptionStatusChangedEventDTO event = new OutpatientReceptionStatusChangedEventDTO();
        event.setReceptionId(reception.getReceptionId());
        event.setReceptionNo(reception.getReceptionNo());
        event.setPatientId(reception.getPatientId());
        event.setPatientName(reception.getPatientName());
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setChangedAt(LocalDateTime.now());
        return event;
    }

    private OutpatientReceptionStatusHistoryDTO toHistoryDto(OutpatientReceptionStatusHistoryEntity entity) {
        OutpatientReceptionStatusHistoryDTO dto = new OutpatientReceptionStatusHistoryDTO();
        dto.setStatusHistoryId(entity.getStatusHistoryId());
        dto.setReceptionId(entity.getReceptionId());
        dto.setFromStatus(entity.getFromStatus() == null ? null : normalizeStatus(entity.getFromStatus()));
        dto.setToStatus(entity.getToStatus() == null ? null : normalizeStatus(entity.getToStatus()));
        dto.setChangedBy(entity.getChangedBy());
        dto.setChangedAt(entity.getChangedAt());
        dto.setReasonCode(entity.getReasonCode());
        dto.setReasonText(entity.getReasonText());
        return dto;
    }

    private OutpatientQualificationSnapshotDTO toQualificationSnapshotDto(ReceptionQualificationSnapshotEntity entity) {
        OutpatientQualificationSnapshotDTO dto = new OutpatientQualificationSnapshotDTO();
        dto.setQualificationSnapshotId(entity.getQualificationSnapshotId());
        dto.setReceptionId(entity.getReceptionId());
        dto.setPatientId(entity.getPatientId());
        dto.setSnapshotDatetime(entity.getSnapshotDatetime());
        dto.setResultCd(entity.getResultCd());
        dto.setPayerTypeCd(entity.getPayerTypeCd());
        dto.setInsuranceTypeCd(entity.getInsuranceTypeCd());
        dto.setValidYn(entity.getValidYn());
        dto.setSourceSystemCd(entity.getSourceSystemCd());
        return dto;
    }

    private OutpatientQualificationItemDTO toQualificationItemDto(ReceptionQualificationItemEntity entity) {
        OutpatientQualificationItemDTO dto = new OutpatientQualificationItemDTO();
        dto.setQualificationItemId(entity.getQualificationItemId());
        dto.setQualificationSnapshotId(entity.getQualificationSnapshotId());
        dto.setItemName(entity.getItemName());
        dto.setItemValue(entity.getItemValue());
        dto.setItemStatusCd(entity.getItemStatusCd());
        dto.setDisplayOrder(entity.getDisplayOrder());
        return dto;
    }

    private OutpatientCallHistoryDTO toCallHistoryDto(ReceptionCallHistoryEntity entity) {
        OutpatientCallHistoryDTO dto = new OutpatientCallHistoryDTO();
        dto.setCallHistoryId(entity.getCallHistoryId());
        dto.setWaitingQueueId(entity.getWaitingQueueId());
        dto.setCallDatetime(entity.getCallDatetime());
        dto.setCallUserId(entity.getCallUserId());
        dto.setCallCount(entity.getCallCount());
        dto.setCallResultCd(entity.getCallResultCd());
        dto.setRemark(entity.getRemark());
        return dto;
    }

    private OutpatientVisitClosureDTO toVisitClosureDto(ReceptionVisitClosureEntity entity) {
        OutpatientVisitClosureDTO dto = new OutpatientVisitClosureDTO();
        dto.setVisitClosureId(entity.getVisitClosureId());
        dto.setReceptionId(entity.getReceptionId());
        dto.setClosureStatusCd(entity.getClosureStatusCd());
        dto.setClosureDatetime(entity.getClosureDatetime());
        dto.setClosureUserId(entity.getClosureUserId());
        dto.setClosureReasonCd(entity.getClosureReasonCd());
        dto.setRemark(entity.getRemark());
        dto.setActiveYn(entity.getActiveYn());
        return dto;
    }

    private OutpatientClosureReasonDTO toClosureReasonDto(ReceptionClosureReasonEntity entity) {
        OutpatientClosureReasonDTO dto = new OutpatientClosureReasonDTO();
        dto.setClosureReasonCd(entity.getClosureReasonCd());
        dto.setClosureReasonName(entity.getClosureReasonName());
        dto.setReasonGroupCd(entity.getReasonGroupCd());
        dto.setUsableYn(entity.getUsableYn());
        dto.setSortOrder(entity.getSortOrder());
        return dto;
    }

    private OutpatientVisitClosureHistoryDTO toVisitClosureHistoryDto(ReceptionVisitClosureHistoryEntity entity) {
        OutpatientVisitClosureHistoryDTO dto = new OutpatientVisitClosureHistoryDTO();
        dto.setVisitClosureHistoryId(entity.getVisitClosureHistoryId());
        dto.setVisitClosureId(entity.getVisitClosureId());
        dto.setBeforeStatusCd(entity.getBeforeStatusCd());
        dto.setAfterStatusCd(entity.getAfterStatusCd());
        dto.setChangedBy(entity.getChangedBy());
        dto.setChangedAt(entity.getChangedAt());
        dto.setChangeReason(entity.getChangeReason());
        return dto;
    }

    private OutpatientSettlementSnapshotDTO toSettlementSnapshotDto(ReceptionSettlementSnapshotEntity entity) {
        OutpatientSettlementSnapshotDTO dto = new OutpatientSettlementSnapshotDTO();
        dto.setSettlementSnapshotId(entity.getSettlementSnapshotId());
        dto.setReceptionId(entity.getReceptionId());
        dto.setPayStatusCd(entity.getPayStatusCd());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setInsuranceAmount(entity.getInsuranceAmount());
        dto.setPatientAmount(entity.getPatientAmount());
        dto.setSnapshotDatetime(entity.getSnapshotDatetime());
        return dto;
    }

    private OutpatientReceptionAuditDTO toReceptionAuditDto(ReceptionAuditEntity entity) {
        OutpatientReceptionAuditDTO dto = new OutpatientReceptionAuditDTO();
        dto.setReceptionAuditId(entity.getReceptionAuditId());
        dto.setReceptionId(entity.getReceptionId());
        dto.setChangeTypeCd(entity.getChangeTypeCd());
        dto.setChangeFieldNm(entity.getChangeFieldNm());
        dto.setBeforeValue(entity.getBeforeValue());
        dto.setAfterValue(entity.getAfterValue());
        dto.setChangeReason(entity.getChangeReason());
        dto.setChangedBy(entity.getChangedBy());
        dto.setChangedAt(entity.getChangedAt());
        return dto;
    }

    private void upsertOutpatientDetail(OutpatientReceptionEntity reception) {
        OutpatientReceptionDetailEntity detail = receptionDetailRepository.findByReceptionId(reception.getReceptionId())
                .orElseGet(OutpatientReceptionDetailEntity::new);

        if (detail.getOutpatientDetailId() == null) {
            detail.setReceptionId(reception.getReceptionId());
        }
        detail.setReservationId(reception.getReservationId());
        detail.setPrimarySymptom(trimToNull(reception.getNote()));
        detail.setVisitPurposeCd(reception.getVisitType());
        detail.setConsultationTypeCd(reception.getVisitType());
        detail.setInsuranceApplyYn("N");
        detail.setActiveYn(toYn(reception.getIsActive()));
        receptionDetailRepository.save(detail);
    }

    private OutpatientWaitingQueueEntity upsertWaitingQueue(OutpatientReceptionEntity reception) {
        OutpatientWaitingQueueEntity queue = waitingQueueRepository.findByReceptionId(reception.getReceptionId())
                .orElseGet(OutpatientWaitingQueueEntity::new);

        if (queue.getWaitingQueueId() == null) {
            queue.setReceptionId(reception.getReceptionId());
            queue.setQueueOrderNo(reception.getReceptionId());
        }
        String queueStatus = mapQueueStatus(reception.getStatus());
        queue.setQueueNo(reception.getReceptionNo());
        queue.setQueueStatusCd(queueStatus);
        queue.setDeptId(reception.getDepartmentId());
        queue.setDoctorId(reception.getDoctorId());
        queue.setActiveYn(toYn(Boolean.TRUE.equals(reception.getIsActive()) && !isQueueClosedStatus(queueStatus)));
        return waitingQueueRepository.save(queue);
    }

    private void syncQualificationSnapshot(OutpatientReceptionEntity reception) {
        ReceptionQualificationSnapshotEntity snapshot = qualificationSnapshotRepository
                .findTopByReceptionIdOrderBySnapshotDatetimeDesc(reception.getReceptionId())
                .orElseGet(ReceptionQualificationSnapshotEntity::new);

        if (snapshot.getQualificationSnapshotId() == null) {
            snapshot.setReceptionId(reception.getReceptionId());
        }
        snapshot.setPatientId(reception.getPatientId());
        snapshot.setSnapshotDatetime(LocalDateTime.now());
        snapshot.setResultCd(Boolean.TRUE.equals(reception.getIsActive()) ? "VALID" : "INVALID");
        snapshot.setPayerTypeCd("SELF");
        snapshot.setInsuranceTypeCd("GENERAL");
        snapshot.setValidYn(toYn(reception.getIsActive()));
        snapshot.setSourceSystemCd("RECEPTION_BACKEND");
        ReceptionQualificationSnapshotEntity saved = qualificationSnapshotRepository.save(snapshot);
        qualificationItemRepository.deleteByQualificationSnapshotId(saved.getQualificationSnapshotId());

        saveQualificationItem(saved.getQualificationSnapshotId(), "STATUS", reception.getStatus(), 1);
        saveQualificationItem(saved.getQualificationSnapshotId(), "VISIT_TYPE", reception.getVisitType(), 2);
        if (trimToNull(reception.getNote()) != null) {
            saveQualificationItem(saved.getQualificationSnapshotId(), "SYMPTOM_SUMMARY", trimToNull(reception.getNote()), 3);
        }
    }

    private void saveQualificationItem(Long snapshotId, String itemName, String itemValue, int displayOrder) {
        ReceptionQualificationItemEntity item = new ReceptionQualificationItemEntity();
        item.setQualificationSnapshotId(snapshotId);
        item.setItemName(itemName);
        item.setItemValue(trimToNull(itemValue));
        item.setItemStatusCd("SYNCED");
        item.setDisplayOrder(displayOrder);
        qualificationItemRepository.save(item);
    }

    private void syncVisitClosure(
            OutpatientReceptionEntity reception,
            String status,
            Long changedBy,
            String reasonCode,
            String reasonText
    ) {
        String nextClosureStatus = mapClosureStatus(status);
        ReceptionVisitClosureEntity closure = visitClosureRepository.findByReceptionId(reception.getReceptionId())
                .orElseGet(ReceptionVisitClosureEntity::new);
        String beforeStatus = closure.getClosureStatusCd();

        if (closure.getVisitClosureId() == null) {
            closure.setReceptionId(reception.getReceptionId());
        }
        closure.setClosureStatusCd(nextClosureStatus);
        closure.setClosureDatetime(LocalDateTime.now());
        closure.setClosureUserId(changedBy);
        closure.setClosureReasonCd(trimToNull(reasonCode));
        closure.setRemark(trimToNull(reasonText));
        closure.setActiveYn("Y");

        if (trimToNull(reasonCode) != null) {
            ensureClosureReason(reasonCode, reasonText);
        }

        ReceptionVisitClosureEntity savedClosure = visitClosureRepository.save(closure);
        if (!Objects.equals(beforeStatus, nextClosureStatus)) {
            ReceptionVisitClosureHistoryEntity history = new ReceptionVisitClosureHistoryEntity();
            history.setVisitClosureId(savedClosure.getVisitClosureId());
            history.setBeforeStatusCd(beforeStatus);
            history.setAfterStatusCd(nextClosureStatus);
            history.setChangedBy(changedBy);
            history.setChangedAt(LocalDateTime.now());
            history.setChangeReason(trimToNull(reasonText));
            visitClosureHistoryRepository.save(history);
        }
    }

    private void ensureClosureReason(String reasonCode, String reasonText) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return;
        }
        closureReasonRepository.findById(reasonCode).orElseGet(() -> {
            ReceptionClosureReasonEntity reason = new ReceptionClosureReasonEntity();
            reason.setClosureReasonCd(reasonCode);
            reason.setClosureReasonName(trimToNull(reasonText) != null ? trimToNull(reasonText) : reasonCode);
            reason.setReasonGroupCd("AUTO");
            reason.setUsableYn("Y");
            reason.setSortOrder(999);
            return closureReasonRepository.save(reason);
        });
    }

    private String mapClosureStatus(String status) {
        String normalized = normalizeStatus(defaultIfBlank(status, "WAITING"));
        return switch (normalized) {
            case "COMPLETED" -> "COMPLETED";
            case "CANCELLED" -> "CANCELLED";
            case "INACTIVE" -> "INACTIVE";
            default -> "OPEN";
        };
    }

    private void snapshotSettlement(OutpatientReceptionEntity reception) {
        ReceptionSettlementSnapshotEntity snapshot = settlementSnapshotRepository
                .findTopByReceptionIdOrderBySnapshotDatetimeDesc(reception.getReceptionId())
                .orElseGet(ReceptionSettlementSnapshotEntity::new);

        if (snapshot.getSettlementSnapshotId() == null) {
            snapshot.setReceptionId(reception.getReceptionId());
        }
        snapshot.setPayStatusCd(mapPayStatus(reception.getStatus()));
        snapshot.setTotalAmount(BigDecimal.ZERO);
        snapshot.setInsuranceAmount(BigDecimal.ZERO);
        snapshot.setPatientAmount(BigDecimal.ZERO);
        snapshot.setSnapshotDatetime(LocalDateTime.now());
        settlementSnapshotRepository.save(snapshot);
    }

    private String mapPayStatus(String status) {
        String normalized = normalizeStatus(defaultIfBlank(status, "WAITING"));
        if ("COMPLETED".equals(normalized)) {
            return "PAID";
        }
        if ("CANCELLED".equals(normalized)) {
            return "CANCELLED";
        }
        return "PENDING";
    }

    private void saveReceptionAudit(
            Long receptionId,
            String changeType,
            String changeField,
            String beforeValue,
            String afterValue,
            String reason,
            Long changedBy
    ) {
        ReceptionAuditEntity audit = new ReceptionAuditEntity();
        audit.setReceptionId(receptionId);
        audit.setChangeTypeCd(changeType);
        audit.setChangeFieldNm(changeField);
        audit.setBeforeValue(trimToNull(beforeValue));
        audit.setAfterValue(trimToNull(afterValue));
        audit.setChangeReason(trimToNull(reason));
        audit.setChangedBy(changedBy);
        receptionAuditRepository.save(audit);
    }

    private String toAuditValue(OutpatientReceptionEntity reception) {
        return "status=" + defaultIfBlank(reception.getStatus(), "UNKNOWN")
                + ",active=" + toYn(reception.getIsActive())
                + ",deptId=" + reception.getDepartmentId()
                + ",doctorId=" + reception.getDoctorId()
                + ",reservationId=" + reception.getReservationId();
    }

    private void saveReservationConversionHistory(
            OutpatientReceptionEntity reception,
            Long changedBy,
            String resultCd,
            String message
    ) {
        if (reception.getReservationId() == null) {
            return;
        }
        ReservationToReceptionHistoryEntity history = new ReservationToReceptionHistoryEntity();
        history.setReservationId(reception.getReservationId());
        history.setReceptionId(reception.getReceptionId());
        history.setConvertedAt(LocalDateTime.now());
        history.setConvertedBy(changedBy);
        history.setResultCd(resultCd);
        history.setMessage(message);
        reservationToReceptionHistoryRepository.save(history);
    }

    private String mapQueueStatus(String status) {
        String normalized = normalizeStatus(defaultIfBlank(status, "WAITING"));
        return switch (normalized) {
            case "WAITING", "TRIAGE", "TRIAGE_IN_PROGRESS", "IN_PROGRESS", "OBSERVATION", "HOLD", "PAYMENT_WAIT", "COMPLETED", "CANCELLED", "INACTIVE" ->
                    normalized;
            default -> "WAITING";
        };
    }

    private boolean isQueueClosedStatus(String queueStatus) {
        return "COMPLETED".equals(queueStatus)
                || "CANCELLED".equals(queueStatus)
                || "INACTIVE".equals(queueStatus);
    }

    private String toYn(Boolean value) {
        if (value == null) {
            return "Y";
        }
        return value ? "Y" : "N";
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

    private void applyStatusSideEffects(OutpatientReceptionEntity entity, String status, String reasonCode, String reasonText) {
        String normalizedStatus = normalizeStatus(status);
        String normalizedReasonCode = trimToNull(reasonCode);
        String normalizedReasonText = trimToNull(reasonText);

        switch (normalizedStatus) {
            case "CANCELLED" -> {
                entity.setIsActive(false);
                entity.setInactiveAt(LocalDateTime.now());
                entity.setCancelReasonCode(normalizedReasonCode);
                entity.setCancelReasonText(normalizedReasonText);
                entity.setInactiveReasonCode(normalizedReasonCode);
                entity.setInactiveReasonText(normalizedReasonText);
                entity.setHoldReasonCode(null);
                entity.setHoldReasonText(null);
            }
            case "INACTIVE" -> {
                entity.setIsActive(false);
                entity.setInactiveAt(LocalDateTime.now());
                entity.setInactiveReasonCode(normalizedReasonCode);
                entity.setInactiveReasonText(normalizedReasonText);
                entity.setCancelReasonCode(null);
                entity.setCancelReasonText(null);
                entity.setHoldReasonCode(null);
                entity.setHoldReasonText(null);
            }
            case "HOLD" -> {
                entity.setIsActive(true);
                entity.setInactiveAt(null);
                entity.setInactiveReasonCode(null);
                entity.setInactiveReasonText(null);
                entity.setCancelReasonCode(null);
                entity.setCancelReasonText(null);
                entity.setHoldReasonCode(normalizedReasonCode);
                entity.setHoldReasonText(normalizedReasonText);
            }
            default -> {
                entity.setIsActive(true);
                entity.setInactiveAt(null);
                entity.setInactiveReasonCode(null);
                entity.setInactiveReasonText(null);
                entity.setCancelReasonCode(null);
                entity.setCancelReasonText(null);
                entity.setHoldReasonCode(null);
                entity.setHoldReasonText(null);
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
            case "진료완료", "TREATMENT_COMPLETED", "PAYMENT_IN_PROGRESS" -> "PAYMENT_WAIT";
            case "수납중" -> "PAYMENT_WAIT";
            case "수납완료", "PAYMENT_COMPLETED" -> "COMPLETED";
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

    private OutpatientReceptionDTO normalizeStatusForResponse(OutpatientReceptionDTO dto) {
        if (dto == null) {
            return null;
        }
        dto.setStatus(normalizeStatus(dto.getStatus()));
        return dto;
    }

    private String resolveReasonCodeForStatus(String status, OutpatientReceptionDTO request, OutpatientReceptionEntity existing) {
        if ("CANCELLED".equals(status)) {
            return firstNonBlank(
                    request == null ? null : request.getCancelReasonCode(),
                    existing == null ? null : existing.getCancelReasonCode(),
                    existing == null ? null : existing.getInactiveReasonCode(),
                    "CANCELLED_BY_USER"
            );
        }
        if ("INACTIVE".equals(status)) {
            return firstNonBlank(
                    request == null ? null : request.getInactiveReasonCode(),
                    existing == null ? null : existing.getInactiveReasonCode()
            );
        }
        if ("HOLD".equals(status)) {
            return firstNonBlank(
                    request == null ? null : request.getHoldReasonCode(),
                    existing == null ? null : existing.getHoldReasonCode()
            );
        }
        return firstNonBlank(
                request == null ? null : request.getInactiveReasonCode(),
                request == null ? null : request.getCancelReasonCode(),
                request == null ? null : request.getHoldReasonCode()
        );
    }

    private String resolveReasonTextForStatus(String status, OutpatientReceptionDTO request, OutpatientReceptionEntity existing) {
        if ("CANCELLED".equals(status)) {
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
            return firstNonBlank(
                    request == null ? null : request.getHoldReasonText(),
                    existing == null ? null : existing.getHoldReasonText()
            );
        }
        return firstNonBlank(
                request == null ? null : request.getInactiveReasonText(),
                request == null ? null : request.getCancelReasonText(),
                request == null ? null : request.getHoldReasonText()
        );
    }

    private PatientRef resolvePatient(Long patientId, String patientName) {
        PatientServiceClient.PatientSummary patientSummary;
        if (patientId != null) {
            patientSummary = patientServiceClient.requirePatientById(patientId);
            String resolvedName = firstNonBlank(patientSummary.patientName(), patientName);
            if (resolvedName == null) {
                throw new IllegalArgumentException("patientName not found for patientId=" + patientId);
            }
            return new PatientRef(patientSummary.patientId(), resolvedName);
        }
        String normalizedPatientName = trimToNull(patientName);
        if (normalizedPatientName == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        patientSummary = patientServiceClient.requirePatientByName(normalizedPatientName);
        return new PatientRef(patientSummary.patientId(), firstNonBlank(patientSummary.patientName(), normalizedPatientName));
    }

    private String resolveDepartmentName(String departmentId, String fallbackName) {
        String normalizedDepartmentId = trimToNull(departmentId);
        String normalizedName = trimToNull(fallbackName);
        if (normalizedName != null) {
            return normalizedName;
        }

        if (normalizedDepartmentId == null) {
            return null;
        }

        return departmentService.findDepartmentName(normalizedDepartmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "departmentName not found for departmentId=" + normalizedDepartmentId
                ));
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

        return doctorService.findDoctorName(normalizedDoctorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "doctorName not found for doctorId=" + normalizedDoctorId
                ));
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

    private String resolveCreateReceptionNo(OutpatientReceptionDTO reception) {
        String requestedReceptionNo = trimToNull(reception.getReceptionNo());
        if (requestedReceptionNo != null) {
            if (receptionRepository.existsByReceptionNo(requestedReceptionNo)) {
                throw new IllegalArgumentException("Duplicated receptionNo: " + requestedReceptionNo);
            }
            return requestedReceptionNo;
        }
        String generatedReceptionNo = receptionNumberSequenceClient.nextReceptionNo("OUTPATIENT");
        if (receptionRepository.existsByReceptionNo(generatedReceptionNo)) {
            throw new IllegalStateException("Generated duplicated receptionNo: " + generatedReceptionNo);
        }
        return generatedReceptionNo;
    }

    private record PatientRef(Long patientId, String patientName) {
    }
}
