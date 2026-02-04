package kr.co.seoulit.reception.mapstruct;

import kr.co.seoulit.common.mapper.EntityReqMapper;
import kr.co.seoulit.reception.dto.ReservationDTO;
import kr.co.seoulit.reception.entity.ReservationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "Spring")
public interface ReservationReqMapStruct
        extends EntityReqMapper<ReservationEntity, ReservationDTO> {
}
