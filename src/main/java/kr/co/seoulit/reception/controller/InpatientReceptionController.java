package kr.co.seoulit.reception.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.dto.InpatientReceptionDTO;
import kr.co.seoulit.reception.service.InpatientReceptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/inpatient-receptions")
@Tag(name = "입원 접수 API", description = "입원 접수 API")
@Slf4j
public class InpatientReceptionController {

    private final InpatientReceptionService inpatientReceptionService;

    @Operation(summary = "입원 접수 목록 조회", description = "입원 접수 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InpatientReceptionDTO>>> getInpatientReceptions(
            @Parameter(description = "검색 유형") @RequestParam(required = false) String searchType,
            @Parameter(description = "검색어") @RequestParam(required = false) String searchValue
    ) {
        log.info("입원 접수 목록 요청: searchType={}, searchValue={}", searchType, searchValue);
        HashMap<String, Object> searchCondition = new HashMap<>();
        searchCondition.put("searchType", searchType);
        searchCondition.put("searchValue", searchValue);

        List<InpatientReceptionDTO> list = inpatientReceptionService.getInpatientReceptionList(searchCondition);
        return ResponseEntity.ok(new ApiResponse<>(true, "입원 접수 목록 조회 완료", list));
    }

    @Operation(summary = "입원 접수 조회", description = "입원 접수 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InpatientReceptionDTO>> getInpatientReception(
            @Parameter(description = "접수 ID") @PathVariable Long id
    ) {
        log.info("입원 접수 조회 요청: id={}", id);
        InpatientReceptionDTO dto = inpatientReceptionService.getInpatientReception(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "입원 접수 조회 완료", dto));
    }

    @Operation(summary = "입원 접수 등록", description = "입원 접수 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> createInpatientReception(
            @RequestBody InpatientReceptionDTO request
    ) {
        log.info("입원 접수 등록 요청: receptionNo={}", request.getReceptionNo());
        inpatientReceptionService.createInpatientReception(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "입원 접수 등록 완료", true));
    }

    @Operation(summary = "입원 접수 수정", description = "입원 접수 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateInpatientReception(
            @Parameter(description = "접수 ID") @PathVariable Long id,
            @RequestBody InpatientReceptionDTO request
    ) {
        log.info("입원 접수 수정 요청: id={}", id);
        inpatientReceptionService.updateInpatientReception(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "입원 접수 수정 완료", true));
    }
}
