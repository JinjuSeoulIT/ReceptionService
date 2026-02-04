package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.dto.InpatientReceptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InpatientReceptionMyBatisMapper {
    List<InpatientReceptionDTO> selectInpatientReceptions(
            @Param("searchType") String searchType,
            @Param("searchValue") String searchValue
    );
}
