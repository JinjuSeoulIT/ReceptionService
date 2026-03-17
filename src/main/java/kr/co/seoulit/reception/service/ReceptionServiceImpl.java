package kr.co.seoulit.reception.service;

import kr.co.seoulit.common.audit.AuditLogService;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusHistoryDTO;
import kr.co.seoulit.reception.outpatient.entity.OutpatientReceptionEntity;
import kr.co.seoulit.reception.outpatient.entity.OutpatientReceptionStatusHistoryEntity;
import kr.co.seoulit.reception.exception.ReceptionNotFoundException;
import kr.co.seoulit.reception.mapstruct.ReceptionReqMapStruct;
import kr.co.seoulit.reception.mapstruct.ReceptionResMapStruct;
import kr.co.seoulit.reception.repository.DepartmentRepository;
import kr.co.seoulit.reception.repository.DoctorRepository;
import kr.co.seoulit.reception.repository.PatientRepository;
import kr.co.seoulit.reception.repository.ReceptionMyBatisMapper;
import kr.co.seoulit.reception.repository.ReceptionRepository;
import kr.co.seoulit.reception.repository.ReceptionStatusHistoryRepository;
import kr.co.seoulit.reception.util.KoreanLabelUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReceptionServiceImpl implements ReceptionService {

    private final ReceptionRepository receptionRepository;
    private final ReceptionMyBatisMapper receptionMyBatisMapper;
    private final ReceptionResMapStruct receptionResMapStruct;
    private final ReceptionReqMapStruct receptionReqMapStruct;
    private final ReceptionStatusHistoryRepository receptionStatusHistoryRepository;
    private final AuditLogService auditLogService;
    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public List<OutpatientReceptionDTO> getReceptionList(Map<String, Object> searchCondition) {
        String searchType = (String) searchCondition.get("searchType");
        String searchValue = (String) searchCondition.get("searchValue");
        String dateFrom = (String) searchCondition.get("dateFrom");
        String dateTo = (String) searchCondition.get("dateTo");
        Long departmentId = (Long) searchCondition.get("departmentId");
        Long doctorId = (Long) searchCondition.get("doctorId");

        try {
            return receptionMyBatisMapper.selectReceptions(
                    searchType,
                    searchValue,
                    dateFrom,
                    dateTo,
                    departmentId,
                    doctorId
            ).stream()
                    .map(KoreanLabelUtil::toKorean)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("selectReceptions failed. fallback to JPA list", ex);
            return fallbackReceptionList(searchType, searchValue, dateFrom, dateTo, departmentId, doctorId);
        }
    }

    @Override
    @Cacheable(key = "#receptionId", value = "RECEPTION")
    public OutpatientReceptionDTO getReception(Long receptionId) {
        OutpatientReceptionEntity entity = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new ReceptionNotFoundException(
                        "접수 ID " + receptionId + "에 해당하는 접수 정보가 없습니다."
                ));
        return KoreanLabelUtil.toKorean(receptionResMapStruct.toDto(entity));
    }

    @Override
    public List<OutpatientReceptionDTO> getReceptionQueue(Long departmentId, Long doctorId, String date) {
        try {
            return receptionMyBatisMapper.selectQueue(departmentId, doctorId, date)
                    .stream()
                    .map(KoreanLabelUtil::toKorean)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("selectQueue failed. fallback to in-memory filter", ex);
            return fallbackQueueList(departmentId, doctorId, date);
        }
    }

    @Override
    @Transactional
    public void createReception(OutpatientReceptionDTO reception) {
        if (reception.getReceptionNo() == null || reception.getReceptionNo().isBlank()) {
            throw new IllegalArgumentException("접수번호는 필수입니다.");
        }
        if ((reception.getPatientId() == null)
                && (reception.getPatientName() == null || reception.getPatientName().isBlank())) {
            throw new IllegalArgumentException("환자 ID 또는 환자 이름이 필요합니다.");
        }
        if (reception.getDepartmentId() == null) {
            throw new IllegalArgumentException("진료과 ID는 필수입니다.");
        }
        if (receptionRepository.existsByReceptionNo(reception.getReceptionNo())) {
            throw new IllegalArgumentException("이미 존재하는 접수번호입니다: " + reception.getReceptionNo());
        }

        OutpatientReceptionEntity entity = receptionReqMapStruct.toEntity(reception);
        entity.setPatientId(resolveOrCreatePatientId(entity.getPatientId(), entity.getPatientName()));
        if (entity.getPatientName() == null || entity.getPatientName().isBlank()) {
            entity.setPatientName(resolvePatientName(entity.getPatientId()));
        }
        if (entity.getDepartmentName() == null || entity.getDepartmentName().isBlank()) {
            entity.setDepartmentName(resolveDepartmentName(entity.getDepartmentId()));
        }
        if (entity.getDoctorId() != null
                && (entity.getDoctorName() == null || entity.getDoctorName().isBlank())) {
            entity.setDoctorName(resolveDoctorName(entity.getDoctorId()));
        }
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("WAITING");
        }
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }
        OutpatientReceptionEntity saved = receptionRepository.save(entity);
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
                .orElseThrow(() -> new ReceptionNotFoundException(
                        "접수 ID " + receptionId + "에 해당하는 접수 정보가 없습니다."
                ));

        OutpatientReceptionDTO before = receptionResMapStruct.toDto(existing);

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
    public OutpatientReceptionDTO updateReceptionStatus(
            Long receptionId,
            String status,
            Long changedBy,
            String reasonCode,
            String reasonText
    ) {
        OutpatientReceptionEntity existing = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new ReceptionNotFoundException(
                        "접수 ID " + receptionId + "에 해당하는 접수 정보가 없습니다."
                ));

        OutpatientReceptionDTO before = receptionResMapStruct.toDto(existing);
        String fromStatus = existing.getStatus();
        existing.setStatus(status);
        OutpatientReceptionEntity saved = receptionRepository.save(existing);

        OutpatientReceptionStatusHistoryEntity history = new OutpatientReceptionStatusHistoryEntity();
        history.setReceptionId(receptionId);
        history.setFromStatus(fromStatus);
        history.setToStatus(status);
        history.setChangedBy(changedBy);
        history.setReasonCode(reasonCode);
        history.setReasonText(reasonText);
        receptionStatusHistoryRepository.save(history);

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

        return KoreanLabelUtil.toKorean(receptionResMapStruct.toDto(saved));
    }

    @Override
    public List<OutpatientReceptionStatusHistoryDTO> getReceptionStatusHistory(Long receptionId) {
        try {
            return receptionStatusHistoryRepository.findByReceptionIdOrderByChangedAtAsc(receptionId)
                    .stream()
                    .map(this::toHistoryDto)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("status-history query failed. receptionId={}", receptionId, ex);
            return List.of();
        }
    }

    private List<OutpatientReceptionDTO> fallbackReceptionList(
            String searchType,
            String searchValue,
            String dateFrom,
            String dateTo,
            Long departmentId,
            Long doctorId
    ) {
        String keyword = searchValue == null ? null : searchValue.trim();
        LocalDate from = parseDate(dateFrom);
        LocalDate to = parseDate(dateTo);

        return StreamSupport.stream(receptionRepository.findAll().spliterator(), false)
                .map(receptionResMapStruct::toDto)
                .filter(dto -> {
                    if (keyword == null || keyword.isBlank()) return true;
                    return switch (searchType) {
                        case "receptionNo" -> keyword.equals(dto.getReceptionNo());
                        case "patientId" -> String.valueOf(dto.getPatientId()).equals(keyword);
                        case "patientName" -> dto.getPatientName() != null && dto.getPatientName().contains(keyword);
                        case "status" -> keyword.equals(dto.getStatus());
                        default -> true;
                    };
                })
                .filter(dto -> departmentId == null || departmentId.equals(dto.getDepartmentId()))
                .filter(dto -> doctorId == null || doctorId.equals(dto.getDoctorId()))
                .filter(dto -> {
                    if (from == null && to == null) return true;
                    if (dto.getCreatedAt() == null) return false;
                    LocalDate createdDate = dto.getCreatedAt().toLocalDate();
                    if (from != null && createdDate.isBefore(from)) return false;
                    return to == null || !createdDate.isAfter(to);
                })
                .sorted((a, b) -> Long.compare(
                        a.getReceptionId() == null ? 0L : a.getReceptionId(),
                        b.getReceptionId() == null ? 0L : b.getReceptionId()
                ))
                .map(KoreanLabelUtil::toKorean)
                .collect(Collectors.toList());
    }

    private List<OutpatientReceptionDTO> fallbackQueueList(Long departmentId, Long doctorId, String date) {
        LocalDate target = parseDate(date);
        return StreamSupport.stream(receptionRepository.findAll().spliterator(), false)
                .map(receptionResMapStruct::toDto)
                .filter(dto -> Boolean.TRUE.equals(dto.getIsActive()))
                .filter(dto -> "WAITING".equals(dto.getStatus()) || "CALLED".equals(dto.getStatus()))
                .filter(dto -> departmentId == null || departmentId.equals(dto.getDepartmentId()))
                .filter(dto -> doctorId == null || doctorId.equals(dto.getDoctorId()))
                .filter(dto -> {
                    if (target == null) return true;
                    if (dto.getCreatedAt() == null) return false;
                    return dto.getCreatedAt().toLocalDate().isEqual(target);
                })
                .sorted((a, b) -> {
                    if (a.getScheduledAt() == null && b.getScheduledAt() == null) return 0;
                    if (a.getScheduledAt() == null) return 1;
                    if (b.getScheduledAt() == null) return -1;
                    return a.getScheduledAt().compareTo(b.getScheduledAt());
                })
                .map(KoreanLabelUtil::toKorean)
                .collect(Collectors.toList());
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            return null;
        }
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
        return KoreanLabelUtil.toKorean(dto);
    }

    private Long resolveOrCreatePatientId(Long patientId, String patientName) {
        if (patientId != null) {
            return patientId;
        }
        if (patientName == null || patientName.isBlank()) {
            throw new IllegalArgumentException("환자 이름이 필요합니다.");
        }
        return patientRepository.findByPatientName(patientName.trim())
                .map(p -> p.getPatientId())
                .orElseGet(() -> {
                    kr.co.seoulit.reception.entity.PatientEntity entity =
                            new kr.co.seoulit.reception.entity.PatientEntity();
                    entity.setPatientName(patientName.trim());
                    return patientRepository.save(entity).getPatientId();
                });
    }

    private String resolvePatientName(Long patientId) {
        return patientRepository.findById(patientId)
                .map(p -> p.getPatientName())
                .orElseThrow(() -> new ReceptionNotFoundException(
                        "환자 ID " + patientId + "에 해당하는 환자 정보를 찾을 수 없습니다."
                ));
    }

    private String resolveDepartmentName(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .map(d -> d.getDepartmentName())
                .orElseThrow(() -> new ReceptionNotFoundException(
                        "진료과 ID " + departmentId + "에 해당하는 진료과 정보를 찾을 수 없습니다."
                ));
    }

    private String resolveDoctorName(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .map(d -> d.getDoctorName())
                .orElseThrow(() -> new ReceptionNotFoundException(
                        "의사 ID " + doctorId + "에 해당하는 의사 정보를 찾을 수 없습니다."
                ));
    }
}
