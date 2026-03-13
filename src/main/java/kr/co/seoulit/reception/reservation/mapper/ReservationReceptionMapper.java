<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/repository/ReservationMyBatisMapper.java
package kr.co.seoulit.reception.repository;

import kr.co.seoulit.reception.dto.ReservationDTO;
========
package kr.co.seoulit.reception.reservation.mapper;

import kr.co.seoulit.reception.reservation.dto.ReservationReceptionDTO;
>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/reservation/mapper/ReservationReceptionMapper.java
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
<<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/outpatient/repository/ReservationMyBatisMapper.java
========




>>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/reservation/mapper/ReservationReceptionMapper.java
