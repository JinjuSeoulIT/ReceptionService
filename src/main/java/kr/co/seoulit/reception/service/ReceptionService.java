package kr.co.seoulit.reception.service;

import kr.co.seoulit.reception.dto.ReceptionDTO;
import kr.co.seoulit.reception.dto.ReceptionStatusHistoryDTO;

import java.util.List;
import java.util.Map;

public interface ReceptionService {
    List<ReceptionDTO> getReceptionList(Map<String, Object> searchCondition);

    ReceptionDTO getReception(Long receptionId);

    List<ReceptionDTO> getReceptionQueue(Long departmentId, Long doctorId, String date);

    void createReception(ReceptionDTO reception);

    void updateReception(Long receptionId, ReceptionDTO reception);

    ReceptionDTO updateReceptionStatus(Long receptionId, String status, Long changedBy, String reasonCode, String reasonText);

    List<ReceptionStatusHistoryDTO> getReceptionStatusHistory(Long receptionId);
}
