package kr.co.seoulit.reception.mapstruct;

<<<<<<< HEAD
import kr.co.seoulit.common.mapper.EntityResMapper;
import kr.co.seoulit.reception.dto.ReservationDTO;
import kr.co.seoulit.reception.entity.ReservationEntity;
=======
import kr.co.seoulit.reception.common.mapper.EntityResMapper;
import kr.co.seoulit.reception.reservation.dto.ReservationReceptionDTO;
import kr.co.seoulit.reception.reservation.entity.ReservationReceptionEntity;
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영)
import org.mapstruct.Mapper;

@Mapper(componentModel = "Spring")
public interface ReservationResMapStruct
        extends EntityResMapper<ReservationEntity, ReservationDTO> {
}
<<<<<<< HEAD
=======



>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영)
