<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/repository/EmergencyReceptionMyBatisMapper.java
package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.dto.EmergencyReceptionDTO;
========
package kr.co.seoulit.reception.emergency.mapper;

import kr.co.seoulit.reception.emergency.dto.EmergencyReceptionDTO;
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/emergency/mapper/EmergencyReceptionMapper.java
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmergencyReceptionMyBatisMapper {
    List<EmergencyReceptionDTO> selectEmergencyReceptions(
            @Param("searchType") String searchType,
            @Param("searchValue") String searchValue
    );
}
<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/repository/EmergencyReceptionMyBatisMapper.java
========




>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/emergency/mapper/EmergencyReceptionMapper.java
