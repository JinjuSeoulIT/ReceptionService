package kr.co.seoulit.reception.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.reservation.dto.ReservationReceptionDTO;
import kr.co.seoulit.reception.reservation.dto.ReservationReceptionStatusUpdateRequest;
import kr.co.seoulit.reception.reservation.service.ReservationReceptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
@Tag(name = "예약 접수", description = "예약 접수 API")
@Slf4j
@Validated
public class ReservationReceptionController {

    private final ReservationReceptionService reservationService;

    @Operation(summary = "예약 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationReceptionDTO>>> getReservations(
            @Parameter(description = "검색 유형") @RequestParam(required = false) String searchType,
            @Parameter(description = "검색어") @RequestParam(required = false) String searchValue
    ) {
        log.info("Get reservations request: searchType={}, searchValue={}", searchType, searchValue);
        HashMap<String, Object> searchCondition = new HashMap<>();
        searchCondition.put("searchType", searchType);
        searchCondition.put("searchValue", searchValue);

        List<ReservationReceptionDTO> list = reservationService.getReservationList(searchCondition);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reservation list fetched", list));
    }

    @Operation(summary = "예약 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationReceptionDTO>> getReservation(
            @Parameter(description = "예약 ID") @PathVariable Long id
    ) {
        log.info("Get reservation request: id={}", id);
        ReservationReceptionDTO dto = reservationService.getReservation(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reservation fetched", dto));
    }

    @Operation(summary = "예약 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> createReservation(@Valid @RequestBody ReservationReceptionDTO reservation) {
        log.info("Create reservation request: reservationNo={}", reservation.getReservationNo());
        reservationService.createReservation(reservation);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reservation created", true));
    }

    @Operation(summary = "예약 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateReservation(
            @Parameter(description = "예약 ID") @PathVariable Long id,
            @Valid @RequestBody ReservationReceptionDTO reservation
    ) {
        log.info("Update reservation request: id={}", id);
        reservationService.updateReservation(id, reservation);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reservation updated", true));
    }

    @Operation(summary = "예약 취소")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> cancelReservation(
            @Parameter(description = "예약 ID") @PathVariable Long id,
            @Parameter(description = "변경자 ID") @RequestParam(required = false) Long changedBy,
            @Parameter(description = "사유 코드") @RequestParam(required = false) String reasonCode,
            @Parameter(description = "사유 내용") @RequestParam(required = false) String reasonText
    ) {
        log.info("Cancel reservation request: id={}", id);
        reservationService.updateReservationStatus(
                id,
                "CANCELED",
                changedBy,
                reasonCode != null ? reasonCode : "USER_CANCEL",
                reasonText != null ? reasonText : "Cancelled from frontend"
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Reservation cancelled", true));
    }

    @Operation(summary = "예약 상태 변경")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ReservationReceptionDTO>> updateReservationStatus(
            @Parameter(description = "예약 ID") @PathVariable Long id,
            @Valid @RequestBody ReservationReceptionStatusUpdateRequest request
    ) {
        log.info("Update reservation status request: id={}, status={}", id, request.getStatus());
        ReservationReceptionDTO updated = reservationService.updateReservationStatus(
                id,
                request.getStatus(),
                request.getChangedBy(),
                request.getReasonCode(),
                request.getReasonText()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Reservation status updated", updated));
    }
}
