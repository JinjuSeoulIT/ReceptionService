package kr.co.seoulit.reception.service;

import kr.co.seoulit.reception.dto.ReservationDTO;

import java.util.List;
import java.util.Map;

public interface ReservationService {
    List<ReservationDTO> getReservationList(Map<String, Object> searchCondition);

    ReservationDTO getReservation(Long reservationId);

    void createReservation(ReservationDTO reservation);

    void updateReservation(Long reservationId, ReservationDTO reservation);

    ReservationDTO updateReservationStatus(Long reservationId, String status, Long changedBy, String reasonCode, String reasonText);
}
