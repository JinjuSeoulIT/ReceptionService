package kr.co.seoulit.reception.outpatient.mapper;

import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OutpatientReceptionMapper {
    List<OutpatientReceptionDTO> selectReceptions(
            @Param("searchType") String searchType,
            @Param("searchValue") String searchValue,
            @Param("dateFrom") String dateFrom,
            @Param("dateTo") String dateTo,
            @Param("departmentId") String departmentId,
            @Param("doctorId") String doctorId
    );

    OutpatientReceptionDTO selectReceptionById(@Param("receptionId") Long receptionId);

    List<OutpatientReceptionDTO> selectQueue(
            @Param("departmentId") String departmentId,
            @Param("doctorId") String doctorId,
            @Param("date") String date
    );
}



