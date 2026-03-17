package kr.co.seoulit.reception.emergency.mapper;

import kr.co.seoulit.reception.emergency.dto.EmergencyReceptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmergencyReceptionMapper {
    List<EmergencyReceptionDTO> selectEmergencyReceptions(
            @Param("searchType") String searchType,
            @Param("searchValue") String searchValue
    );
}




