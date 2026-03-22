package kr.co.seoulit.reception.reservation.mapper;

import kr.co.seoulit.reception.reservation.dto.ReservationReceptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReservationReceptionMapper {
    List<ReservationReceptionDTO> selectReservations(
            @Param("searchType") String searchType,
            @Param("searchValue") String searchValue
    );

    ReservationReceptionDTO selectReservationById(@Param("reservationId") Long reservationId);
}



