package kr.co.seoulit.reception.outpatient.service;

import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionAuditDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientCallHistoryDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientClosureReasonDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientQualificationItemDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientQualificationSnapshotDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientSettlementSnapshotDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionStatusHistoryDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientVisitClosureDTO;
import kr.co.seoulit.reception.outpatient.dto.OutpatientVisitClosureHistoryDTO;

import java.util.List;
import java.util.Map;

public interface OutpatientReceptionService {
    List<OutpatientReceptionDTO> getReceptionList(Map<String, Object> searchCondition);

    OutpatientReceptionDTO getReception(Long receptionId);

    List<OutpatientReceptionDTO> getReceptionQueue(String departmentId, String doctorId, String date);

    void createReception(OutpatientReceptionDTO reception);

    void updateReception(Long receptionId, OutpatientReceptionDTO reception);

    OutpatientReceptionDTO updateReceptionStatus(Long receptionId, String status, Long changedBy, String reasonCode, String reasonText);

    List<OutpatientReceptionStatusHistoryDTO> getReceptionStatusHistory(Long receptionId);

    List<OutpatientQualificationSnapshotDTO> getQualificationSnapshots(Long receptionId);

    List<OutpatientQualificationItemDTO> getLatestQualificationItems(Long receptionId);

    List<OutpatientCallHistoryDTO> getCallHistory(Long receptionId);

    OutpatientVisitClosureDTO getVisitClosure(Long receptionId);

    List<OutpatientClosureReasonDTO> getClosureReasons();

    List<OutpatientVisitClosureHistoryDTO> getVisitClosureHistory(Long receptionId);

    List<OutpatientSettlementSnapshotDTO> getSettlementSnapshots(Long receptionId);

    List<OutpatientReceptionAuditDTO> getReceptionAudits(Long receptionId);
}
