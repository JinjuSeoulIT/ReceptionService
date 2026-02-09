package kr.co.seoulit.reception.service;

import kr.co.seoulit.common.audit.AuditLogService;
import kr.co.seoulit.reception.dto.ReservationDTO;
import kr.co.seoulit.reception.entity.PatientEntity;
import kr.co.seoulit.reception.entity.ReservationEntity;
import kr.co.seoulit.reception.mapstruct.ReservationReqMapStruct;
import kr.co.seoulit.reception.mapstruct.ReservationResMapStruct;
import kr.co.seoulit.reception.repository.DepartmentRepository;
import kr.co.seoulit.reception.repository.DoctorRepository;
import kr.co.seoulit.reception.repository.PatientRepository;
import kr.co.seoulit.reception.repository.ReservationMyBatisMapper;
import kr.co.seoulit.reception.repository.ReservationRepository;
import kr.co.seoulit.reception.util.KoreanLabelUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMyBatisMapper reservationMyBatisMapper;
    private final ReservationResMapStruct reservationResMapStruct;
    private final ReservationReqMapStruct reservationReqMapStruct;
    private final AuditLogService auditLogService;
    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public List<ReservationDTO> getReservationList(Map<String, Object> searchCondition) {
        String searchType = (String) searchCondition.get("searchType");
        String searchValue = (String) searchCondition.get("searchValue");
        return reservationMyBatisMapper.selectReservations(searchType, searchValue)
                .stream()
                .map(KoreanLabelUtil::toKorean)
                .toList();
    }

    @Override
    @Cacheable(key = "#reservationId", value = "RESERVATION")
    public ReservationDTO getReservation(Long reservationId) {
        ReservationEntity entity = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 ID를 찾을 수 없습니다: " + reservationId));
        return KoreanLabelUtil.toKorean(reservationResMapStruct.toDto(entity));
    }

    @Override
    @Transactional
    public void createReservation(ReservationDTO reservation) {
        if (reservation.getReservationNo() == null || reservation.getReservationNo().isBlank()) {
            throw new IllegalArgumentException("예약번호는 필수입니다.");
        }
        if (reservationRepository.existsByReservationNo(reservation.getReservationNo())) {
            throw new IllegalArgumentException("이미 존재하는 예약번호입니다: " + reservation.getReservationNo());
        }
        if (reservation.getReservedAt() == null) {
            throw new IllegalArgumentException("예약 시간은 필수입니다.");
        }

        ReservationEntity entity = reservationReqMapStruct.toEntity(reservation);
        entity.setPatientId(resolveOrCreatePatientId(entity.getPatientId(), entity.getPatientName()));
        if (entity.getPatientName() == null || entity.getPatientName().isBlank()) {
            String resolvedName = resolvePatientNameOrNull(entity.getPatientId());
            if (resolvedName != null && !resolvedName.isBlank()) {
                entity.setPatientName(resolvedName);
            }
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
        ReservationEntity saved = reservationRepository.save(entity);
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
    public void updateReservation(Long reservationId, ReservationDTO reservation) {
        ReservationEntity existing = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 ID를 찾을 수 없습니다: " + reservationId));

        ReservationDTO before = reservationResMapStruct.toDto(existing);

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
            String resolvedName = resolvePatientNameOrNull(reservation.getPatientId());
            if (resolvedName != null && !resolvedName.isBlank()) {
                existing.setPatientName(resolvedName);
            }
        }
        if (reservation.getDepartmentId() != null) {
            existing.setDepartmentId(reservation.getDepartmentId());
        }
        if (reservation.getDepartmentName() != null) {
            existing.setDepartmentName(reservation.getDepartmentName());
        } else if (reservation.getDepartmentId() != null) {
            String resolvedDept = resolveDepartmentNameOrNull(reservation.getDepartmentId());
            if (resolvedDept != null && !resolvedDept.isBlank()) {
                existing.setDepartmentName(resolvedDept);
            }
        }
        if (reservation.getDoctorId() != null) {
            existing.setDoctorId(reservation.getDoctorId());
        }
        if (reservation.getDoctorName() != null) {
            existing.setDoctorName(reservation.getDoctorName());
        } else if (reservation.getDoctorId() != null) {
            String resolvedDoctor = resolveDoctorNameOrNull(reservation.getDoctorId());
            if (resolvedDoctor != null && !resolvedDoctor.isBlank()) {
                existing.setDoctorName(resolvedDoctor);
            }
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

        ReservationEntity saved = reservationRepository.save(existing);
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
    public ReservationDTO updateReservationStatus(
            Long reservationId,
            String status,
            Long changedBy,
            String reasonCode,
            String reasonText
    ) {
        ReservationEntity existing = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 ID를 찾을 수 없습니다: " + reservationId));

        ReservationDTO before = reservationResMapStruct.toDto(existing);

        existing.setStatus(status);
        existing.setUpdatedBy(changedBy);
        if ("CANCELED".equalsIgnoreCase(status)) {
            existing.setCanceledAt(LocalDateTime.now());
            existing.setCancelReasonCode(reasonCode);
            existing.setCancelReasonText(reasonText);
        }
        if ("INACTIVE".equalsIgnoreCase(status)) {
            existing.setIsActive(false);
            existing.setInactiveAt(LocalDateTime.now());
            existing.setInactiveReasonCode(reasonCode);
            existing.setInactiveReasonText(reasonText);
        }

        ReservationEntity saved = reservationRepository.save(existing);
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

        return KoreanLabelUtil.toKorean(reservationResMapStruct.toDto(saved));
    }

    private String resolvePatientNameOrNull(Long patientId) {
        return patientRepository.findById(patientId)
                .map(PatientEntity::getPatientName)
                .orElse(null);
    }

    private Long resolveOrCreatePatientId(Long patientId, String patientName) {
        if (patientId != null) {
            return patientId;
        }
        if (patientName == null || patientName.isBlank()) {
            throw new IllegalArgumentException("환자 이름은 필수입니다.");
        }
        return patientRepository.findByPatientName(patientName.trim())
                .map(PatientEntity::getPatientId)
                .orElseGet(() -> {
                    PatientEntity entity = new PatientEntity();
                    entity.setPatientName(patientName.trim());
                    return patientRepository.save(entity).getPatientId();
                });
    }

    private String resolveDepartmentName(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .map(d -> d.getDepartmentName())
                .orElseThrow(() -> new IllegalArgumentException("진료과 ID를 찾을 수 없습니다: " + departmentId));
    }

    private String resolveDoctorName(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .map(d -> d.getDoctorName())
                .orElseThrow(() -> new IllegalArgumentException("의사 ID를 찾을 수 없습니다: " + doctorId));
    }

    private String resolveDepartmentNameOrNull(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .map(d -> d.getDepartmentName())
                .orElse(null);
    }

    private String resolveDoctorNameOrNull(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .map(d -> d.getDoctorName())
                .orElse(null);
    }
}
