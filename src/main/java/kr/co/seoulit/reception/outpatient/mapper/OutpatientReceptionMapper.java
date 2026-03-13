<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/repository/ReceptionMyBatisMapper.java
package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.dto.ReceptionDTO;
========
package kr.co.seoulit.reception.outpatient.mapper;

import kr.co.seoulit.reception.outpatient.dto.OutpatientReceptionDTO;
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/outpatient/mapper/OutpatientReceptionMapper.java
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
<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/repository/ReceptionMyBatisMapper.java
========




>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/outpatient/mapper/OutpatientReceptionMapper.java
