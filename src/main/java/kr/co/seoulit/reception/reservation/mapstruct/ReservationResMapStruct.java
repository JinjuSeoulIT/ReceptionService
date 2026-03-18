package kr.co.seoulit.reception.reservation.mapstruct;

import kr.co.seoulit.common.mapper.EntityResMapper;
import kr.co.seoulit.reception.reservation.dto.ReservationReceptionDTO;
import kr.co.seoulit.reception.reservation.entity.ReservationReceptionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "Spring")
public interface ReservationResMapStruct
        extends EntityResMapper<ReservationReceptionEntity, ReservationReceptionDTO> {
}


