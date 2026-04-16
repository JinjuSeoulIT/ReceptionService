package kr.co.seoulit.reception.reservation.service;

import kr.co.seoulit.reception.reservation.dto.ReservationReceptionDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReservationReceptionService {
    List<ReservationReceptionDTO> getReservationList(Map<String, Object> searchCondition);

    ReservationReceptionDTO getReservation(Long reservationId);

    List<ReservationReceptionDTO> getAutoSyncReservations(LocalDate targetDate);

    void createReservation(ReservationReceptionDTO reservation);

    void updateReservation(Long reservationId, ReservationReceptionDTO reservation);

    ReservationReceptionDTO updateReservationStatus(Long reservationId, String status, Long changedBy, String reasonCode, String reasonText);

    ReservationReceptionDTO completeReservationByAutoSync(Long reservationId);
}
