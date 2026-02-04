package kr.co.seoulit.reception.mapstruct;

import kr.co.seoulit.common.mapper.EntityReqMapper;
import kr.co.seoulit.reception.dto.ReceptionDTO;
import kr.co.seoulit.reception.entity.ReceptionEntity;
import org.mapstruct.Mapper;


@Mapper(componentModel = "Spring")
public interface ReceptionReqMapStruct
        extends EntityReqMapper<ReceptionEntity, ReceptionDTO> {

}