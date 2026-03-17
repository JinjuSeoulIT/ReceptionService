package kr.co.seoulit.reception.service;

import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusHistoryDTO;

import java.util.List;
import java.util.Map;

public interface ReceptionService {
    List<OutpatientReceptionDTO> getReceptionList(Map<String, Object> searchCondition);

    OutpatientReceptionDTO getReception(Long receptionId);

    List<OutpatientReceptionDTO> getReceptionQueue(Long departmentId, Long doctorId, String date);

    void createReception(OutpatientReceptionDTO reception);

    void updateReception(Long receptionId, OutpatientReceptionDTO reception);

    OutpatientReceptionDTO updateReceptionStatus(Long receptionId, String status, Long changedBy, String reasonCode, String reasonText);

    List<OutpatientReceptionStatusHistoryDTO> getReceptionStatusHistory(Long receptionId);
}
