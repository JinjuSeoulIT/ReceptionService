package kr.co.seoulit.reception.service;

import kr.co.seoulit.reception.dto.InpatientReceptionDTO;
import kr.co.seoulit.reception.entity.ReceptionEntity;
import kr.co.seoulit.reception.entity.ReceptionInpatientEntity;
import kr.co.seoulit.reception.repository.DepartmentRepository;
import kr.co.seoulit.reception.repository.DoctorRepository;
import kr.co.seoulit.reception.repository.PatientRepository;
import kr.co.seoulit.reception.repository.InpatientReceptionMyBatisMapper;
import kr.co.seoulit.reception.repository.ReceptionInpatientRepository;
import kr.co.seoulit.reception.repository.ReceptionRepository;
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
public class InpatientReceptionServiceImpl implements InpatientReceptionService {

    private final ReceptionRepository receptionRepository;
    private final ReceptionInpatientRepository inpatientRepository;
    private final InpatientReceptionMyBatisMapper inpatientMyBatisMapper;
    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public List<InpatientReceptionDTO> getInpatientReceptionList(Map<String, Object> searchCondition) {
        String searchType = (String) searchCondition.get("searchType");
        String searchValue = (String) searchCondition.get("searchValue");
        return inpatientMyBatisMapper.selectInpatientReceptions(searchType, searchValue);
    }

    @Override
    public InpatientReceptionDTO getInpatientReception(Long receptionId) {
        ReceptionEntity reception = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("?낆썝 ?묒닔 ID " + receptionId + "에 해당하는 데이터가 없습니다."));
        ReceptionInpatientEntity inpatient = inpatientRepository.findById(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("?낆썝 ?묒닔 ?곸꽭 ?뺣낫媛 ?놁뒿?덈떎. receptionId=" + receptionId));

        return toDto(reception, inpatient);
    }

    @Override
    @Transactional
    public void createInpatientReception(InpatientReceptionDTO request) {
        if (request.getReceptionNo() == null || request.getReceptionNo().isBlank()) {
            throw new IllegalArgumentException("?묒닔踰덊샇???꾩닔?낅땲??");
        }
        if (request.getPatientId() == null) {
            throw new IllegalArgumentException("?섏옄 ID???꾩닔?낅땲??");
        }
        if (request.getDepartmentId() == null) {
            throw new IllegalArgumentException("吏꾨즺怨?ID???꾩닔?낅땲??");
        }
        if (request.getAdmissionPlanAt() == null) {
            throw new IllegalArgumentException("?낆썝 ?덉젙?쇱떆???꾩닔?낅땲??");
        }

        ReceptionEntity reception = new ReceptionEntity();
        reception.setReceptionNo(request.getReceptionNo());
        reception.setPatientId(request.getPatientId());
        reception.setPatientName(resolvePatientName(request.getPatientId(), request.getPatientName()));
        reception.setVisitType("INPATIENT");
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

        ReceptionInpatientEntity inpatient = new ReceptionInpatientEntity();
        inpatient.setReceptionId(saved.getReceptionId());
        inpatient.setAdmissionPlanAt(request.getAdmissionPlanAt());
        inpatient.setWardId(request.getWardId());
        inpatient.setRoomId(request.getRoomId());

        inpatientRepository.save(inpatient);
    }

    @Override
    @Transactional
    public void updateInpatientReception(Long receptionId, InpatientReceptionDTO request) {
        ReceptionEntity reception = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("?낆썝 ?묒닔 ID " + receptionId + "에 해당하는 데이터가 없습니다."));
        ReceptionInpatientEntity inpatient = inpatientRepository.findById(receptionId)
                .orElseThrow(() -> new IllegalArgumentException("?낆썝 ?묒닔 ?곸꽭 ?뺣낫媛 ?놁뒿?덈떎. receptionId=" + receptionId));

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

        if (request.getAdmissionPlanAt() != null) {
            inpatient.setAdmissionPlanAt(request.getAdmissionPlanAt());
        }
        if (request.getWardId() != null) {
            inpatient.setWardId(request.getWardId());
        }
        if (request.getRoomId() != null) {
            inpatient.setRoomId(request.getRoomId());
        }

        receptionRepository.save(reception);
        inpatientRepository.save(inpatient);
    }

    private InpatientReceptionDTO toDto(ReceptionEntity reception, ReceptionInpatientEntity inpatient) {
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
        dto.setWardId(inpatient.getWardId());
        dto.setRoomId(inpatient.getRoomId());
        return dto;
    }

    private String resolvePatientName(Long patientId, String fallback) {
        if (fallback != null && !fallback.isBlank()) return fallback;
        return patientRepository.findById(patientId)
                .map(p -> p.getPatientName())
                .orElseThrow(() -> new IllegalArgumentException("?섏옄 ID " + patientId + "???????롫뮉 ?怨쀬뵠?怨? ??곷뮸??덈뼄."));
    }

    private String resolveDepartmentName(Long departmentId, String fallback) {
        if (fallback != null && !fallback.isBlank()) return fallback;
        return departmentRepository.findById(departmentId)
                .map(d -> d.getDepartmentName())
                .orElseThrow(() -> new IllegalArgumentException("吏꾨즺怨?ID " + departmentId + "???????롫뮉 ?怨쀬뵠?怨? ??곷뮸??덈뼄."));
    }

    private String resolveDoctorName(Long doctorId, String fallback) {
        if (doctorId == null) return null;
        if (fallback != null && !fallback.isBlank()) return fallback;
        return doctorRepository.findById(doctorId)
                .map(d -> d.getDoctorName())
                .orElseThrow(() -> new IllegalArgumentException("?섏궗 ID " + doctorId + "???????롫뮉 ?怨쀬뵠?怨? ??곷뮸??덈뼄."));
    }
}
