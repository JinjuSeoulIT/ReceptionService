package kr.co.seoulit.reception.service;

import kr.co.seoulit.reception.dto.EmergencyReceptionDTO;
import kr.co.seoulit.reception.entity.ReceptionEmergencyEntity;
import kr.co.seoulit.reception.entity.ReceptionEntity;
import kr.co.seoulit.reception.repository.DepartmentRepository;
import kr.co.seoulit.reception.repository.DoctorRepository;
import kr.co.seoulit.reception.repository.EmergencyReceptionMyBatisMapper;
import kr.co.seoulit.reception.repository.PatientRepository;
import kr.co.seoulit.reception.repository.ReceptionEmergencyRepository;
import kr.co.seoulit.reception.repository.ReceptionRepository;
import kr.co.seoulit.reception.util.KoreanLabelUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmergencyReceptionServiceImpl implements EmergencyReceptionService {

    private final ReceptionRepository receptionRepository;
    private final ReceptionEmergencyRepository emergencyRepository;
    private final EmergencyReceptionMyBatisMapper emergencyMyBatisMapper;
    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public List<EmergencyReceptionDTO> getEmergencyReceptionList(Map<String, Object> searchCondition) {
        String searchType = (String) searchCondition.get("searchType");
        String searchValue = (String) searchCondition.get("searchValue");
        return emergencyMyBatisMapper.selectEmergencyReceptions(searchType, searchValue)
                .stream()
                .map(KoreanLabelUtil::toKorean)
                .toList();
    }

    @Override
    public EmergencyReceptionDTO getEmergencyReception(Long receptionId) {
        ReceptionEntity reception = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "응급 접수 ID " + receptionId + "에 해당하는 접수 정보가 없습니다."
                ));
        ReceptionEmergencyEntity emergency = emergencyRepository.findById(receptionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "응급 접수 상세가 존재하지 않습니다. receptionId=" + receptionId
                ));

        return KoreanLabelUtil.toKorean(toDto(reception, emergency));
    }

    @Override
    @Transactional
    public void createEmergencyReception(EmergencyReceptionDTO request) {
        if (request.getReceptionNo() == null || request.getReceptionNo().isBlank()) {
            throw new IllegalArgumentException("접수번호는 필수입니다.");
        }
        if (request.getPatientId() == null) {
            throw new IllegalArgumentException("환자 ID는 필수입니다.");
        }
        if (request.getDepartmentId() == null) {
            throw new IllegalArgumentException("진료과 ID는 필수입니다.");
        }
        if (request.getTriageLevel() == null
                || request.getChiefComplaint() == null
                || request.getChiefComplaint().isBlank()) {
            throw new IllegalArgumentException("중증도 단계와 주호소는 필수입니다.");
        }

        ReceptionEntity reception = new ReceptionEntity();
        reception.setReceptionNo(request.getReceptionNo());
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
        reception.setStatus(request.getStatus() != null ? request.getStatus() : "WAITING");
        reception.setNote(request.getNote());
        reception.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        ReceptionEntity saved = receptionRepository.save(reception);

        ReceptionEmergencyEntity emergency = new ReceptionEmergencyEntity();
        emergency.setReceptionId(saved.getReceptionId());
        emergency.setTriageLevel(request.getTriageLevel());
        emergency.setChiefComplaint(request.getChiefComplaint());
        emergency.setVitalTemp(request.getVitalTemp());
        emergency.setVitalBpSystolic(request.getVitalBpSystolic());
        emergency.setVitalBpDiastolic(request.getVitalBpDiastolic());
        emergency.setVitalHr(request.getVitalHr());
        emergency.setVitalRr(request.getVitalRr());
        emergency.setVitalSpo2(request.getVitalSpo2());
        emergency.setArrivalMode(request.getArrivalMode());
        emergency.setTriageNote(request.getTriageNote());

        emergencyRepository.save(emergency);
    }

    @Override
    @Transactional
    public void updateEmergencyReception(Long receptionId, EmergencyReceptionDTO request) {
        ReceptionEntity reception = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "응급 접수 ID " + receptionId + "에 해당하는 접수 정보가 없습니다."
                ));
        ReceptionEmergencyEntity emergency = emergencyRepository.findById(receptionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "응급 접수 상세가 존재하지 않습니다. receptionId=" + receptionId
                ));

        if (request.getReceptionNo() != null && !request.getReceptionNo().isBlank()) {
            reception.setReceptionNo(request.getReceptionNo());
        }
        if (request.getPatientId() != null) {
            reception.setPatientId(request.getPatientId());
        }
        if (request.getPatientName() != null) {
            reception.setPatientName(request.getPatientName());
        } else if (request.getPatientId() != null) {
            reception.setPatientName(resolvePatientName(request.getPatientId(), null));
        }
        if (request.getDepartmentId() != null) {
            reception.setDepartmentId(request.getDepartmentId());
        }
        if (request.getDepartmentName() != null) {
            reception.setDepartmentName(request.getDepartmentName());
        } else if (request.getDepartmentId() != null) {
            reception.setDepartmentName(resolveDepartmentName(request.getDepartmentId(), null));
        }
        if (request.getDoctorId() != null) {
            reception.setDoctorId(request.getDoctorId());
        }
        if (request.getDoctorName() != null) {
            reception.setDoctorName(request.getDoctorName());
        } else if (request.getDoctorId() != null) {
            reception.setDoctorName(resolveDoctorName(request.getDoctorId(), null));
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

        if (request.getTriageLevel() != null) {
            emergency.setTriageLevel(request.getTriageLevel());
        }
        if (request.getChiefComplaint() != null) {
            emergency.setChiefComplaint(request.getChiefComplaint());
        }
        if (request.getVitalTemp() != null) {
            emergency.setVitalTemp(request.getVitalTemp());
        }
        if (request.getVitalBpSystolic() != null) {
            emergency.setVitalBpSystolic(request.getVitalBpSystolic());
        }
        if (request.getVitalBpDiastolic() != null) {
            emergency.setVitalBpDiastolic(request.getVitalBpDiastolic());
        }
        if (request.getVitalHr() != null) {
            emergency.setVitalHr(request.getVitalHr());
        }
        if (request.getVitalRr() != null) {
            emergency.setVitalRr(request.getVitalRr());
        }
        if (request.getVitalSpo2() != null) {
            emergency.setVitalSpo2(request.getVitalSpo2());
        }
        if (request.getArrivalMode() != null) {
            emergency.setArrivalMode(request.getArrivalMode());
        }
        if (request.getTriageNote() != null) {
            emergency.setTriageNote(request.getTriageNote());
        }

        receptionRepository.save(reception);
        emergencyRepository.save(emergency);
    }

    private EmergencyReceptionDTO toDto(ReceptionEntity reception, ReceptionEmergencyEntity emergency) {
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
        dto.setTriageLevel(emergency.getTriageLevel());
        dto.setChiefComplaint(emergency.getChiefComplaint());
        dto.setVitalTemp(emergency.getVitalTemp());
        dto.setVitalBpSystolic(emergency.getVitalBpSystolic());
        dto.setVitalBpDiastolic(emergency.getVitalBpDiastolic());
        dto.setVitalHr(emergency.getVitalHr());
        dto.setVitalRr(emergency.getVitalRr());
        dto.setVitalSpo2(emergency.getVitalSpo2());
        dto.setArrivalMode(emergency.getArrivalMode());
        dto.setTriageNote(emergency.getTriageNote());
        return dto;
    }

    private String resolvePatientName(Long patientId, String fallback) {
        if (fallback != null && !fallback.isBlank()) return fallback;
        return patientRepository.findById(patientId)
                .map(p -> p.getPatientName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "환자 ID " + patientId + "에 해당하는 환자 정보를 찾을 수 없습니다."
                ));
    }

    private String resolveDepartmentName(Long departmentId, String fallback) {
        if (fallback != null && !fallback.isBlank()) return fallback;
        return departmentRepository.findById(departmentId)
                .map(d -> d.getDepartmentName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "진료과 ID " + departmentId + "에 해당하는 진료과 정보를 찾을 수 없습니다."
                ));
    }

    private String resolveDoctorName(Long doctorId, String fallback) {
        if (doctorId == null) return null;
        if (fallback != null && !fallback.isBlank()) return fallback;
        return doctorRepository.findById(doctorId)
                .map(d -> d.getDoctorName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "의사 ID " + doctorId + "에 해당하는 의사 정보를 찾을 수 없습니다."
                ));
    }
}
