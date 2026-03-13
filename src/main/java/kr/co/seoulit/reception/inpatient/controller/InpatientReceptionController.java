<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/controller/InpatientReceptionController.java
package kr.co.seoulit.reception.controller;
=======
package kr.co.seoulit.reception.inpatient.controller;
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/inpatient/controller/InpatientReceptionController.java

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/controller/InpatientReceptionController.java
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.dto.InpatientReceptionDTO;
import kr.co.seoulit.reception.service.InpatientReceptionService;
=======
import kr.co.seoulit.reception.common.api.ApiResponse;
import kr.co.seoulit.reception.inpatient.dto.InpatientReceptionDTO;
import kr.co.seoulit.reception.inpatient.service.InpatientReceptionService;
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/inpatient/controller/InpatientReceptionController.java
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
<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/controller/InpatientReceptionController.java
@Tag(name = "입원 접수 API", description = "입원 접수 API")
=======
@Tag(name = "입원 접수", description = "입원 접수 기능")
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/inpatient/controller/InpatientReceptionController.java
@Slf4j
public class InpatientReceptionController {

    private final InpatientReceptionService inpatientReceptionService;

<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/controller/InpatientReceptionController.java
    @Operation(summary = "입원 접수 목록 조회", description = "입원 접수 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InpatientReceptionDTO>>> getInpatientReceptions(
            @Parameter(description = "검색 유형") @RequestParam(required = false) String searchType,
=======
    @Operation(summary = "입원 접수 목록 조회", description = "입원 접수 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InpatientReceptionDTO>>> getInpatientReceptions(
            @Parameter(description = "검색 구분") @RequestParam(required = false) String searchType,
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/inpatient/controller/InpatientReceptionController.java
            @Parameter(description = "검색어") @RequestParam(required = false) String searchValue
    ) {
        log.info("입원 접수 목록 요청: searchType={}, searchValue={}", searchType, searchValue);
        HashMap<String, Object> searchCondition = new HashMap<>();
        searchCondition.put("searchType", searchType);
        searchCondition.put("searchValue", searchValue);

        List<InpatientReceptionDTO> list = inpatientReceptionService.getInpatientReceptionList(searchCondition);
        return ResponseEntity.ok(new ApiResponse<>(true, "입원 접수 목록 조회 완료", list));
    }

<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/controller/InpatientReceptionController.java
    @Operation(summary = "입원 접수 조회", description = "입원 접수 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InpatientReceptionDTO>> getInpatientReception(
            @Parameter(description = "접수 ID") @PathVariable Long id
=======
    @Operation(summary = "입원 접수 상세 조회", description = "입원 접수 상세 1건을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InpatientReceptionDTO>> getInpatientReception(
            @Parameter(description = "접수 식별자") @PathVariable Long id
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/inpatient/controller/InpatientReceptionController.java
    ) {
        log.info("입원 접수 조회 요청: id={}", id);
        InpatientReceptionDTO dto = inpatientReceptionService.getInpatientReception(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "입원 접수 조회 완료", dto));
    }

<<<<<<< HEAD:src/main/java/kr/co/seoulit/reception/controller/InpatientReceptionController.java
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
=======
    @Operation(summary = "입원 접수 등록", description = "입원 접수를 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> createInpatientReception(@RequestBody InpatientReceptionDTO request) {
        log.info("입원 Create reception request: receptionNo={}", request.getReceptionNo());
        inpatientReceptionService.createInpatientReception(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inpatient reception created", true));
    }

    @Operation(summary = "입원 접수 수정", description = "입원 접수를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateInpatientReception(
            @Parameter(description = "접수 식별자") @PathVariable Long id,
            @RequestBody InpatientReceptionDTO request
    ) {
        log.info("입원 Update reception request: id={}", id);
        inpatientReceptionService.updateInpatientReception(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inpatient reception updated", true));
    }

    @Operation(summary = "입원 접수 취소", description = "입원 접수를 취소 처리합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> cancelInpatientReception(
            @Parameter(description = "접수 식별자") @PathVariable Long id
    ) {
        log.info("입원 Cancel reception request: id={}", id);
        InpatientReceptionDTO request = new InpatientReceptionDTO();
        request.setStatus("CANCELLED");
        request.setIsActive(false);
        request.setNote("Cancelled from frontend");
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/inpatient/controller/InpatientReceptionController.java
        inpatientReceptionService.updateInpatientReception(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "입원 접수 수정 완료", true));
    }
}


