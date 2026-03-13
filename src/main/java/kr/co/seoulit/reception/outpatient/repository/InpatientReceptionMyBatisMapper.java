<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/repository/InpatientReceptionMyBatisMapper.java
package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.dto.InpatientReceptionDTO;
========
package kr.co.seoulit.reception.inpatient.mapper;

import kr.co.seoulit.reception.inpatient.dto.InpatientReceptionDTO;
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/inpatient/mapper/InpatientReceptionMapper.java
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
<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/repository/InpatientReceptionMyBatisMapper.java
========




>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/inpatient/mapper/InpatientReceptionMapper.java
