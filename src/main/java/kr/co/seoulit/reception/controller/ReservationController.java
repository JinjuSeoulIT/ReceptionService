package kr.co.seoulit.reception.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.reservation.dto.ReservationReceptionDTO;
import kr.co.seoulit.reception.reservation.dto.ReservationReceptionStatusUpdateRequest;
import kr.co.seoulit.reception.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
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
@Tag(name = "예약 API", description = "예약 API")
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "예약 목록 조회", description = "예약 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationReceptionDTO>>> getReservations(
            @Parameter(description = "검색 유형") @RequestParam(required = false) String searchType,
            @Parameter(description = "검색어") @RequestParam(required = false) String searchValue
    ) {
        log.info("예약 목록 요청: searchType={}, searchValue={}", searchType, searchValue);
        HashMap<String, Object> searchCondition = new HashMap<>();
        searchCondition.put("searchType", searchType);
        searchCondition.put("searchValue", searchValue);

        List<ReservationReceptionDTO> list = reservationService.getReservationList(searchCondition);
        return ResponseEntity.ok(new ApiResponse<>(true, "예약 목록 조회 완료", list));
    }

    @Operation(summary = "예약 조회", description = "예약 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationReceptionDTO>> getReservation(
            @Parameter(description = "예약 ID") @PathVariable Long id
    ) {
        log.info("예약 조회 요청: id={}", id);
        ReservationReceptionDTO dto = reservationService.getReservation(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "예약 조회 완료", dto));
    }

    @Operation(summary = "예약 등록", description = "예약 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> createReservation(@RequestBody ReservationReceptionDTO reservation) {
        log.info("예약 등록 요청: reservationNo={}", reservation.getReservationNo());
        reservationService.createReservation(reservation);
        return ResponseEntity.ok(new ApiResponse<>(true, "예약 등록 완료", true));
    }

    @Operation(summary = "예약 수정", description = "예약 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateReservation(
            @Parameter(description = "예약 ID") @PathVariable Long id,
            @RequestBody ReservationReceptionDTO reservation
    ) {
        log.info("예약 수정 요청: id={}", id);
        reservationService.updateReservation(id, reservation);
        return ResponseEntity.ok(new ApiResponse<>(true, "예약 수정 완료", true));
    }

    @Operation(summary = "예약 상태 변경", description = "예약 상태 변경")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ReservationReceptionDTO>> updateReservationStatus(
            @Parameter(description = "예약 ID") @PathVariable Long id,
            @RequestBody ReservationReceptionStatusUpdateRequest request
    ) {
        log.info("예약 상태 변경 요청: id={}, status={}", id, request.getStatus());
        ReservationReceptionDTO updated = reservationService.updateReservationStatus(
                id,
                request.getStatus(),
                request.getChangedBy(),
                request.getReasonCode(),
                request.getReasonText()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "예약 상태 변경 완료", updated));
    }
}
