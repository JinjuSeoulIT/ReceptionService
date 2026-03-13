package kr.co.seoulit.reception.outpatient.service;

import kr.co.seoulit.reception.common.audit.AuditLogService;
import kr.co.seoulit.reception.exception.ReceptionNotFoundException;
import kr.co.seoulit.reception.mapstruct.ReceptionReqMapStruct;
import kr.co.seoulit.reception.mapstruct.ReceptionResMapStruct;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusHistoryDTO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutpatientReceptionServiceImpl implements OutpatientReceptionService {

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
    private final AuditLogService auditLogService;

    @Override
    public List<OutpatientReceptionDTO> getReceptionList(Map<String, Object> searchCondition) {
        String searchType = (String) searchCondition.get("searchType");
        String searchValue = (String) searchCondition.get("searchValue");
        String dateFrom = (String) searchCondition.get("dateFrom");
        String dateTo = (String) searchCondition.get("dateTo");
        Long departmentId = (Long) searchCondition.get("departmentId");
        Long doctorId = (Long) searchCondition.get("doctorId");

        return receptionMyBatisMapper.selectReceptions(
                searchType,
                searchValue,
                dateFrom,
                dateTo,
                departmentId,
                doctorId
        );
    }

    @Override
    @Cacheable(key = "#receptionId", value = "RECEPTION")
    public OutpatientReceptionDTO getReception(Long receptionId) {
        OutpatientReceptionEntity entity = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new ReceptionNotFoundException("Reception not found. receptionId=" + receptionId));
        return receptionResMapStruct.toDto(entity);
    }

    @Override
    public List<OutpatientReceptionDTO> getReceptionQueue(Long departmentId, Long doctorId, String date) {
        return receptionMyBatisMapper.selectQueue(departmentId, doctorId, date);
    }

    @Override
    @Transactional
    public void createReception(OutpatientReceptionDTO reception) {
        if (reception.getReceptionNo() == null || reception.getReceptionNo().isBlank()) {
            throw new IllegalArgumentException("receptionNo is required");
        }
        if ((reception.getPatientId() == null)
                && (reception.getPatientName() == null || reception.getPatientName().isBlank())) {
            throw new IllegalArgumentException("patientId or patientName is required");
        }
        if (reception.getDepartmentId() == null) {
            throw new IllegalArgumentException("departmentId is required");
        }
        if (receptionRepository.existsByReceptionNo(reception.getReceptionNo())) {
            throw new IllegalArgumentException("Duplicated receptionNo: " + reception.getReceptionNo());
        }

        OutpatientReceptionEntity entity = receptionReqMapStruct.toEntity(reception);
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
            entity.setStatus("WAITING");
        }
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }

        OutpatientReceptionEntity saved = receptionRepository.save(entity);
        upsertOutpatientDetail(saved);
        OutpatientWaitingQueueEntity queue = upsertWaitingQueue(saved);
        syncQualificationSnapshot(saved);
        syncVisitClosure(saved, saved.getStatus(), reception.getCreatedBy(), null, null);
        snapshotSettlement(saved);
        saveReceptionAudit(
                saved.getReceptionId(),
                "CREATE",
                "*",
                null,
                toAuditValue(saved),
                null,
                reception.getCreatedBy()
        );
        saveReservationConversionHistory(saved, reception.getCreatedBy(), "SUCCESS", "Reception created");
        maybeInsertCallHistory(saved, queue, reception.getCreatedBy());

        auditLogService.log(
                "RECEPTION",
                saved.getReceptionId(),
                "CREATE",
                reception.getCreatedBy(),
                null,
                null,
                null,
                receptionResMapStruct.toDto(saved)
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "RECEPTION", key = "#receptionId")
    public void updateReception(Long receptionId, OutpatientReceptionDTO reception) {
        OutpatientReceptionEntity existing = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new ReceptionNotFoundException("Reception not found. receptionId=" + receptionId));

        OutpatientReceptionDTO before = receptionResMapStruct.toDto(existing);
        String beforeAuditValue = toAuditValue(existing);
        Long beforeReservationId = existing.getReservationId();

        if (reception.getReceptionNo() != null && !reception.getReceptionNo().isBlank()) {
            existing.setReceptionNo(reception.getReceptionNo());
        }
        if (reception.getPatientId() != null) {
            existing.setPatientId(reception.getPatientId());
        }
        if (reception.getPatientName() != null) {
            existing.setPatientName(reception.getPatientName());
            if (reception.getPatientId() == null) {
                existing.setPatientId(resolveOrCreatePatientId(null, reception.getPatientName()));
            }
        } else if (reception.getPatientId() != null) {
            existing.setPatientName(resolvePatientName(reception.getPatientId()));
        }
        if (reception.getVisitType() != null && !reception.getVisitType().isBlank()) {
            existing.setVisitType(reception.getVisitType());
        }
        if (reception.getDepartmentId() != null) {
            existing.setDepartmentId(reception.getDepartmentId());
        }
        if (reception.getDepartmentName() != null) {
            existing.setDepartmentName(reception.getDepartmentName());
        } else if (reception.getDepartmentId() != null) {
            existing.setDepartmentName(resolveDepartmentName(reception.getDepartmentId()));
        }
        if (reception.getDoctorId() != null) {
            existing.setDoctorId(reception.getDoctorId());
        }
        if (reception.getDoctorName() != null) {
            existing.setDoctorName(reception.getDoctorName());
        } else if (reception.getDoctorId() != null) {
            existing.setDoctorName(resolveDoctorName(reception.getDoctorId()));
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
        if (reception.getIsActive() != null) {
            existing.setIsActive(reception.getIsActive());
        }
        if (reception.getInactiveAt() != null) {
            existing.setInactiveAt(reception.getInactiveAt());
        }
        if (reception.getInactiveReasonCode() != null) {
            existing.setInactiveReasonCode(reception.getInactiveReasonCode());
        }
        if (reception.getInactiveReasonText() != null) {
            existing.setInactiveReasonText(reception.getInactiveReasonText());
        }
        if (reception.getCancelReasonCode() != null) {
            existing.setCancelReasonCode(reception.getCancelReasonCode());
        }
        if (reception.getCancelReasonText() != null) {
            existing.setCancelReasonText(reception.getCancelReasonText());
        }
        if (reception.getHoldReasonCode() != null) {
            existing.setHoldReasonCode(reception.getHoldReasonCode());
        }
        if (reception.getHoldReasonText() != null) {
            existing.setHoldReasonText(reception.getHoldReasonText());
        }
        if (reception.getCreatedBy() != null) {
            existing.setCreatedBy(reception.getCreatedBy());
        }
        if (reception.getUpdatedBy() != null) {
            existing.setUpdatedBy(reception.getUpdatedBy());
        }
        if (reception.getStatus() != null && !reception.getStatus().isBlank()) {
            existing.setStatus(reception.getStatus());
        }

        OutpatientReceptionEntity saved = receptionRepository.save(existing);
        upsertOutpatientDetail(saved);
        OutpatientWaitingQueueEntity queue = upsertWaitingQueue(saved);
        syncQualificationSnapshot(saved);
        syncVisitClosure(
                saved,
                saved.getStatus(),
                reception.getUpdatedBy(),
                firstNonBlank(saved.getCancelReasonCode(), saved.getInactiveReasonCode()),
                firstNonBlank(saved.getCancelReasonText(), saved.getInactiveReasonText())
        );
        snapshotSettlement(saved);
        saveReceptionAudit(
                saved.getReceptionId(),
                "UPDATE",
                "*",
                beforeAuditValue,
                toAuditValue(saved),
                firstNonBlank(saved.getCancelReasonText(), saved.getInactiveReasonText()),
                reception.getUpdatedBy()
        );
        if (beforeReservationId == null && saved.getReservationId() != null) {
            saveReservationConversionHistory(saved, reception.getUpdatedBy(), "SUCCESS", "Reservation linked");
        }
        maybeInsertCallHistory(saved, queue, reception.getUpdatedBy());

        auditLogService.log(
                "RECEPTION",
                saved.getReceptionId(),
                "UPDATE",
                reception.getUpdatedBy(),
                null,
                null,
                before,
                receptionResMapStruct.toDto(saved)
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "RECEPTION", key = "#receptionId")
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
        String fromStatus = existing.getStatus();
        existing.setStatus(status);
        existing.setUpdatedBy(changedBy);

        if ("CANCELLED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
            existing.setIsActive(false);
            existing.setInactiveAt(LocalDateTime.now());
            existing.setCancelReasonCode(reasonCode);
            existing.setCancelReasonText(reasonText);
            existing.setInactiveReasonCode(reasonCode);
            existing.setInactiveReasonText(reasonText);
        } else if ("INACTIVE".equalsIgnoreCase(status)) {
            existing.setIsActive(false);
            existing.setInactiveAt(LocalDateTime.now());
            existing.setInactiveReasonCode(reasonCode);
            existing.setInactiveReasonText(reasonText);
        }

        OutpatientReceptionEntity saved = receptionRepository.save(existing);
        upsertOutpatientDetail(saved);
        OutpatientWaitingQueueEntity queue = upsertWaitingQueue(saved);
        syncQualificationSnapshot(saved);
        syncVisitClosure(saved, status, changedBy, reasonCode, reasonText);
        snapshotSettlement(saved);
        maybeInsertCallHistory(saved, queue, changedBy);

        OutpatientReceptionStatusHistoryEntity history = new OutpatientReceptionStatusHistoryEntity();
        history.setReceptionId(receptionId);
        history.setFromStatus(fromStatus);
        history.setToStatus(status);
        history.setChangedBy(changedBy);
        history.setReasonCode(reasonCode);
        history.setReasonText(reasonText);
        receptionStatusHistoryRepository.save(history);

        saveReceptionAudit(
                saved.getReceptionId(),
                "STATUS_CHANGE",
                "STATUS",
                beforeAuditValue,
                toAuditValue(saved),
                reasonText,
                changedBy
        );

        auditLogService.log(
                "RECEPTION",
                saved.getReceptionId(),
                "STATUS_CHANGE",
                changedBy,
                reasonCode,
                reasonText,
                before,
                receptionResMapStruct.toDto(saved)
        );

        return receptionResMapStruct.toDto(saved);
    }

    private OutpatientReceptionStatusHistoryDTO toHistoryDto(OutpatientReceptionStatusHistoryEntity entity) {
        OutpatientReceptionStatusHistoryDTO dto = new OutpatientReceptionStatusHistoryDTO();
        dto.setStatusHistoryId(entity.getStatusHistoryId());
        dto.setReceptionId(entity.getReceptionId());
        dto.setFromStatus(entity.getFromStatus());
        dto.setToStatus(entity.getToStatus());
        dto.setChangedBy(entity.getChangedBy());
        dto.setChangedAt(entity.getChangedAt());
        dto.setReasonCode(entity.getReasonCode());
        dto.setReasonText(entity.getReasonText());
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
        ReceptionQualificationSnapshotEntity snapshot = new ReceptionQualificationSnapshotEntity();
        snapshot.setReceptionId(reception.getReceptionId());
        snapshot.setPatientId(reception.getPatientId());
        snapshot.setSnapshotDatetime(LocalDateTime.now());
        snapshot.setResultCd(Boolean.TRUE.equals(reception.getIsActive()) ? "VALID" : "INVALID");
        snapshot.setPayerTypeCd("SELF");
        snapshot.setInsuranceTypeCd("GENERAL");
        snapshot.setValidYn(toYn(reception.getIsActive()));
        snapshot.setSourceSystemCd("RECEPTION_BACKEND");
        ReceptionQualificationSnapshotEntity saved = qualificationSnapshotRepository.save(snapshot);

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
        if (status == null || status.isBlank()) {
            return "OPEN";
        }
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "COMPLETED", "DONE" -> "COMPLETED";
            case "CANCELLED", "CANCELED" -> "CANCELLED";
            case "INACTIVE" -> "INACTIVE";
            default -> "OPEN";
        };
    }

    private void snapshotSettlement(OutpatientReceptionEntity reception) {
        ReceptionSettlementSnapshotEntity snapshot = new ReceptionSettlementSnapshotEntity();
        snapshot.setReceptionId(reception.getReceptionId());
        snapshot.setPayStatusCd(mapPayStatus(reception.getStatus()));
        snapshot.setTotalAmount(BigDecimal.ZERO);
        snapshot.setInsuranceAmount(BigDecimal.ZERO);
        snapshot.setPatientAmount(BigDecimal.ZERO);
        snapshot.setSnapshotDatetime(LocalDateTime.now());
        settlementSnapshotRepository.save(snapshot);
    }

    private String mapPayStatus(String status) {
        if (status == null || status.isBlank()) {
            return "PENDING";
        }
        String normalized = status.trim().toUpperCase();
        if ("COMPLETED".equals(normalized) || "DONE".equals(normalized)) {
            return "PAID";
        }
        if ("CANCELLED".equals(normalized) || "CANCELED".equals(normalized)) {
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

    private void maybeInsertCallHistory(
            OutpatientReceptionEntity reception,
            OutpatientWaitingQueueEntity queue,
            Long changedBy
    ) {
        if (queue == null || queue.getWaitingQueueId() == null) {
            return;
        }
        if (!"CALLED".equalsIgnoreCase(reception.getStatus())) {
            return;
        }

        int callCount = callHistoryRepository.findTopByWaitingQueueIdOrderByCallDatetimeDesc(queue.getWaitingQueueId())
                .map(item -> item.getCallCount() == null ? 1 : item.getCallCount() + 1)
                .orElse(1);

        ReceptionCallHistoryEntity history = new ReceptionCallHistoryEntity();
        history.setWaitingQueueId(queue.getWaitingQueueId());
        history.setCallDatetime(LocalDateTime.now());
        history.setCallUserId(changedBy);
        history.setCallCount(callCount);
        history.setCallResultCd("CALLED");
        history.setRemark("Auto generated by status change");
        callHistoryRepository.save(history);
    }

    private String mapQueueStatus(String status) {
        if (status == null || status.isBlank()) {
            return "WAITING";
        }
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "WAITING", "CALLED", "IN_PROGRESS", "COMPLETED", "DONE", "CANCELLED", "CANCELED", "INACTIVE" ->
                    normalized;
            default -> "WAITING";
        };
    }

    private boolean isQueueClosedStatus(String queueStatus) {
        return "COMPLETED".equals(queueStatus)
                || "DONE".equals(queueStatus)
                || "CANCELLED".equals(queueStatus)
                || "CANCELED".equals(queueStatus)
                || "INACTIVE".equals(queueStatus);
    }

    private String toYn(Boolean value) {
        if (value == null) {
            return "Y";
        }
        return value ? "Y" : "N";
    }

    private String firstNonBlank(String first, String second) {
        return trimToNull(first) != null ? trimToNull(first) : trimToNull(second);
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
