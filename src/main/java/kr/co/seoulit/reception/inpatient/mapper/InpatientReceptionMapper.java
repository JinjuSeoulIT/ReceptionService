package kr.co.seoulit.reception.inpatient.mapper;

import kr.co.seoulit.reception.inpatient.dto.InpatientReceptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InpatientReceptionMapper {
    List<InpatientReceptionDTO> selectInpatientReceptions(
            @Param("searchType") String searchType,
            @Param("searchValue") String searchValue
    );
}




