package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.dto.ReceptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReceptionMyBatisMapper {
    List<ReceptionDTO> selectReceptions(
            @Param("searchType") String searchType,
            @Param("searchValue") String searchValue,
            @Param("dateFrom") String dateFrom,
            @Param("dateTo") String dateTo,
            @Param("departmentId") Long departmentId,
            @Param("doctorId") Long doctorId
    );

    List<ReceptionDTO> selectQueue(
            @Param("departmentId") Long departmentId,
            @Param("doctorId") Long doctorId,
            @Param("date") String date
    );
}
