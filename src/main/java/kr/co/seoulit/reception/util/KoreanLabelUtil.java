package kr.co.seoulit.reception.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kr.co.seoulit.reception.emergency.dto.EmergencyReceptionDTO;
import kr.co.seoulit.reception.dto.InpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusHistoryDTO;
import kr.co.seoulit.reception.reservation.dto.ReservationReceptionDTO;

public final class KoreanLabelUtil {

    private static final Map<String, String> STATUS_LABEL = new HashMap<>();
    private static final Map<String, String> VISIT_TYPE_LABEL = new HashMap<>();
    private static final Map<Long, String> DEPARTMENT_BY_ID = new HashMap<>();
    private static final Map<String, String> DEPARTMENT_BY_NAME = new HashMap<>();

    static {
        STATUS_LABEL.put("WAITING", "대기");
        STATUS_LABEL.put("TRIAGE", "분류중");
        STATUS_LABEL.put("IN_PROGRESS", "진료중");
        STATUS_LABEL.put("OBSERVATION", "관찰중");
        STATUS_LABEL.put("COMPLETED", "완료");
        STATUS_LABEL.put("TRANSFERRED", "전원");
        STATUS_LABEL.put("ON_HOLD", "보류");
        STATUS_LABEL.put("CANCELED", "취소");

        // Legacy aliases for backward compatibility
        STATUS_LABEL.put("CALLED", "분류중");
        STATUS_LABEL.put("PAYMENT_WAIT", "관찰중");
        STATUS_LABEL.put("INACTIVE", "전원");
        STATUS_LABEL.put("RESERVED", "예약");

        VISIT_TYPE_LABEL.put("OUTPATIENT", "외래");
        VISIT_TYPE_LABEL.put("EMERGENCY", "응급");
        VISIT_TYPE_LABEL.put("INPATIENT", "입원");

        DEPARTMENT_BY_ID.put(1L, "내과");
        DEPARTMENT_BY_ID.put(2L, "외과");
        DEPARTMENT_BY_ID.put(3L, "정형외과");
        DEPARTMENT_BY_ID.put(4L, "신경외과");

        DEPARTMENT_BY_NAME.put("internal medicine", "내과");
        DEPARTMENT_BY_NAME.put("surgery", "외과");
        DEPARTMENT_BY_NAME.put("orthopedics", "정형외과");
        DEPARTMENT_BY_NAME.put("neurosurgery", "신경외과");
        DEPARTMENT_BY_NAME.put("emergency", "내과");
    }

    private KoreanLabelUtil() {}

    public static String toKoreanStatus(String status) {
        if (status == null || status.isBlank()) return status;
        String key = status.trim().toUpperCase(Locale.ROOT);
        return STATUS_LABEL.getOrDefault(key, status);
    }

    public static String toKoreanVisitType(String visitType) {
        if (visitType == null || visitType.isBlank()) return visitType;
        String key = visitType.trim().toUpperCase(Locale.ROOT);
        return VISIT_TYPE_LABEL.getOrDefault(key, visitType);
    }

    public static String toKoreanDepartmentName(String name, Long departmentId) {
        String trimmed = name == null ? null : name.trim();
        if (trimmed != null && !trimmed.isBlank()) {
            if (trimmed.startsWith("진료과") && departmentId != null) {
                return DEPARTMENT_BY_ID.getOrDefault(departmentId, trimmed);
            }
            String key = trimmed.toLowerCase(Locale.ROOT);
            String mapped = DEPARTMENT_BY_NAME.get(key);
            return mapped != null ? mapped : trimmed;
        }
        if (departmentId != null) {
            return DEPARTMENT_BY_ID.getOrDefault(departmentId, "진료과" + departmentId);
        }
        return trimmed;
    }

    public static OutpatientReceptionDTO toKorean(OutpatientReceptionDTO dto) {
        if (dto == null) return null;
        dto.setStatus(toKoreanStatus(dto.getStatus()));
        dto.setVisitType(toKoreanVisitType(dto.getVisitType()));
        dto.setDepartmentName(toKoreanDepartmentName(dto.getDepartmentName(), dto.getDepartmentId()));
        return dto;
    }

    public static EmergencyReceptionDTO toKorean(EmergencyReceptionDTO dto) {
        if (dto == null) return null;
        dto.setStatus(toKoreanStatus(dto.getStatus()));
        dto.setVisitType(toKoreanVisitType(dto.getVisitType()));
        dto.setDepartmentName(toKoreanDepartmentName(dto.getDepartmentName(), dto.getDepartmentId()));
        return dto;
    }

    public static InpatientReceptionDTO toKorean(InpatientReceptionDTO dto) {
        if (dto == null) return null;
        dto.setStatus(toKoreanStatus(dto.getStatus()));
        dto.setVisitType(toKoreanVisitType(dto.getVisitType()));
        dto.setDepartmentName(toKoreanDepartmentName(dto.getDepartmentName(), dto.getDepartmentId()));
        return dto;
    }

    public static ReservationReceptionDTO toKorean(ReservationReceptionDTO dto) {
        if (dto == null) return null;
        dto.setStatus(toKoreanStatus(dto.getStatus()));
        dto.setDepartmentName(toKoreanDepartmentName(dto.getDepartmentName(), dto.getDepartmentId()));
        return dto;
    }

    public static OutpatientReceptionStatusHistoryDTO toKorean(OutpatientReceptionStatusHistoryDTO dto) {
        if (dto == null) return null;
        dto.setFromStatus(toKoreanStatus(dto.getFromStatus()));
        dto.setToStatus(toKoreanStatus(dto.getToStatus()));
        return dto;
    }
}
