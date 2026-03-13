package kr.co.seoulit.reception.emergency.service;

import kr.co.seoulit.reception.emergency.dto.EmergencyReceptionDTO;

import java.util.List;
import java.util.Map;

public interface EmergencyReceptionService {
    List<EmergencyReceptionDTO> getEmergencyReceptionList(Map<String, Object> searchCondition);

    EmergencyReceptionDTO getEmergencyReception(Long receptionId);

    void createEmergencyReception(EmergencyReceptionDTO request);

    void updateEmergencyReception(Long receptionId, EmergencyReceptionDTO request);
}
