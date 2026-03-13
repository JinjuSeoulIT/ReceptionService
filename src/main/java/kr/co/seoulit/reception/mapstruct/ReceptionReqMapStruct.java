package kr.co.seoulit.reception.mapstruct;

<<<<<<< HEAD
import kr.co.seoulit.common.mapper.EntityReqMapper;
import kr.co.seoulit.reception.dto.ReceptionDTO;
import kr.co.seoulit.reception.entity.ReceptionEntity;
=======
import kr.co.seoulit.reception.common.mapper.EntityReqMapper;
import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import kr.co.seoulit.reception.outpatient.entity.OutpatientReceptionEntity;
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영)
import org.mapstruct.Mapper;


@Mapper(componentModel = "Spring")
public interface ReceptionReqMapStruct
        extends EntityReqMapper<ReceptionEntity, ReceptionDTO> {

<<<<<<< HEAD
}
=======

>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영)
