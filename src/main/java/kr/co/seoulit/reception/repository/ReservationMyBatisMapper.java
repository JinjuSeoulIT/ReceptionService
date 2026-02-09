package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.dto.ReservationDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReservationMyBatisMapper {
    List<ReservationDTO> selectReservations(
            @Param("searchType") String searchType,
            @Param("searchValue") String searchValue
    );
}
