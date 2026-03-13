package kr.co.seoulit.reception.inpatient.service;

import kr.co.seoulit.reception.inpatient.dto.InpatientReceptionDTO;

import java.util.List;
import java.util.Map;

public interface InpatientReceptionService {
    List<InpatientReceptionDTO> getInpatientReceptionList(Map<String, Object> searchCondition);

    InpatientReceptionDTO getInpatientReception(Long receptionId);

    void createInpatientReception(InpatientReceptionDTO request);

    void updateInpatientReception(Long receptionId, InpatientReceptionDTO request);
}
