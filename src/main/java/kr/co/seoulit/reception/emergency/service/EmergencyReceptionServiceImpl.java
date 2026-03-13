package kr.co.seoulit.reception.emergency.service;

import kr.co.seoulit.reception.emergency.dto.EmergencyReceptionDTO;
import kr.co.seoulit.reception.emergency.entity.EmergencyReceptionEntity;
import kr.co.seoulit.reception.emergency.entity.EmergencyTriageEntity;
import kr.co.seoulit.reception.emergency.mapper.EmergencyReceptionMapper;
import kr.co.seoulit.reception.emergency.repository.EmergencyReceptionRepository;
import kr.co.seoulit.reception.emergency.repository.EmergencyTriageRepository;
import kr.co.seoulit.reception.outpatient.entity.OutpatientReceptionEntity;
import kr.co.seoulit.reception.outpatient.repository.OutpatientReceptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmergencyReceptionServiceImpl implements EmergencyReceptionService {

    private final OutpatientReceptionRepository receptionRepository;
    private final EmergencyReceptionRepository emergencyRepository;
    private final EmergencyTriageRepository emergencyTriageRepository;
    private final EmergencyReceptionMapper emergencyMyBatisMapper;

    @Override
    public List<EmergencyReceptionDTO> getEmergencyReceptionList(Map<String, Object> searchCondition) {
        String searchType = (String) searchCondition.get("searchType");
        String searchValue = (String) searchCondition.get("searchValue");
        return emergencyMyBatisMapper.selectEmergencyReceptions(searchType, searchValue);
    }

    @Override
    public EmergencyReceptionDTO getEmergencyReception(Long receptionId) {
        OutpatientReceptionEntity reception = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("응급 접수 정보를 찾을 수 없습니다. receptionId=" + receptionId));
        EmergencyReceptionEntity emergency = emergencyRepository.findByReceptionId(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("응급 상세 정보를 찾을 수 없습니다. receptionId=" + receptionId));
        EmergencyTriageEntity triage = emergencyTriageRepository
                .findTopByReceptionIdOrderByTriageDatetimeDesc(receptionId)
                .orElse(null);

        return toEmergencyDto(reception, emergency, triage);
    }

    @Override
    @Transactional
    public void createEmergencyReception(EmergencyReceptionDTO request) {
        if (request.getReceptionNo() == null || request.getReceptionNo().isBlank()) {
            throw new IllegalArgumentException("접수 번호는 필수입니다.");
        }
        if (request.getPatientId() == null) {
            throw new IllegalArgumentException("환자 ID는 필수입니다.");
        }
        if (request.getDepartmentId() == null) {
            throw new IllegalArgumentException("진료과 ID는 필수입니다.");
        }
        if (request.getTriageLevel() == null || request.getChiefComplaint() == null || request.getChiefComplaint().isBlank()) {
            throw new IllegalArgumentException("응급 분류 단계와 주호소는 필수입니다.");
        }

        OutpatientReceptionEntity reception = new OutpatientReceptionEntity();
        reception.setReceptionNo(request.getReceptionNo());
        reception.setPatientId(request.getPatientId());
        reception.setPatientName(resolvePatientNameWithFallback(request.getPatientId(), request.getPatientName()));
        reception.setVisitType("EMERGENCY");
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

        OutpatientReceptionEntity saved = receptionRepository.save(reception);

        EmergencyReceptionEntity emergency = new EmergencyReceptionEntity();
        emergency.setReceptionId(saved.getReceptionId());
        emergency.setChiefComplaint(request.getChiefComplaint());
        emergency.setVitalTemp(request.getVitalTemp());
        emergency.setBloodPressure(toBloodPressure(request.getVitalBpSystolic(), request.getVitalBpDiastolic()));
        emergency.setVitalHr(request.getVitalHr());
        emergency.setArrivalMode(request.getArrivalMode());
        emergency.setArrivalDatetime(request.getArrivedAt());
        emergency.setActiveYn(toYn(request.getIsActive()));
        emergencyRepository.save(emergency);

        EmergencyTriageEntity triage = new EmergencyTriageEntity();
        triage.setReceptionId(saved.getReceptionId());
        triage.setTriageLevelCd(toTriageLevelCode(request.getTriageLevel()));
        triage.setTriageDatetime(request.getArrivedAt() != null ? request.getArrivedAt() : LocalDateTime.now());
        triage.setTriageNote(request.getTriageNote());
        triage.setActiveYn(toYn(request.getIsActive()));
        emergencyTriageRepository.save(triage);
    }

    @Override
    @Transactional
    public void updateEmergencyReception(Long receptionId, EmergencyReceptionDTO request) {
        OutpatientReceptionEntity reception = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("응급 접수 정보를 찾을 수 없습니다. receptionId=" + receptionId));
        EmergencyReceptionEntity emergency = emergencyRepository.findByReceptionId(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("응급 상세 정보를 찾을 수 없습니다. receptionId=" + receptionId));
        EmergencyTriageEntity triage = emergencyTriageRepository
                .findTopByReceptionIdOrderByTriageDatetimeDesc(receptionId)
                .orElse(null);

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
        if (request.getIsActive() != null) {
            emergency.setActiveYn(toYn(request.getIsActive()));
        }

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
            triage.setActiveYn(toYn(request.getIsActive()));
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
        dto.setStatus(reception.getStatus());
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

    private String resolvePatientNameWithFallback(Long patientId, String fallback) {
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        if (patientId == null) {
            throw new IllegalArgumentException("환자 ID는 필수입니다.");
        }
        return "환자-" + patientId;
    }

    private String resolveDepartmentNameWithFallback(Long departmentId, String fallback) {
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        if (departmentId == null) {
            throw new IllegalArgumentException("진료과 ID는 필수입니다.");
        }
        return "진료과-" + departmentId;
    }

    private String resolveDoctorNameWithFallback(Long doctorId, String fallback) {
        if (doctorId == null) {
            return null;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return "의사-" + doctorId;
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
}
