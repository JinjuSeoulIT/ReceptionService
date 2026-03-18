package kr.co.seoulit.reception.outpatient.mapstruct;

import kr.co.seoulit.common.mapper.EntityReqMapper;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.entity.OutpatientReceptionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "Spring")
public interface ReceptionReqMapStruct
        extends EntityReqMapper<OutpatientReceptionEntity, OutpatientReceptionDTO> {
}
