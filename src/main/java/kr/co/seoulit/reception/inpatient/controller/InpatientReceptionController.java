package kr.co.seoulit.reception.inpatient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.inpatient.dto.InpatientReceptionDTO;
import kr.co.seoulit.reception.inpatient.service.InpatientReceptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@Tag(name = "Inpatient Reception", description = "Inpatient reception APIs")
@Slf4j
@Validated
public class InpatientReceptionController {

    private final InpatientReceptionService inpatientReceptionService;

    @Operation(summary = "Get inpatient receptions")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InpatientReceptionDTO>>> getInpatientReceptions(
            @Parameter(description = "Search type") @RequestParam(required = false) String searchType,
            @Parameter(description = "Search value") @RequestParam(required = false) String searchValue
    ) {
        log.info("Get inpatient receptions request: searchType={}, searchValue={}", searchType, searchValue);
        HashMap<String, Object> searchCondition = new HashMap<>();
        searchCondition.put("searchType", searchType);
        searchCondition.put("searchValue", searchValue);

        List<InpatientReceptionDTO> list = inpatientReceptionService.getInpatientReceptionList(searchCondition);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inpatient reception list fetched", list));
    }

    @Operation(summary = "Get inpatient reception detail")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InpatientReceptionDTO>> getInpatientReception(
            @Parameter(description = "Reception id") @PathVariable Long id
    ) {
        log.info("Get inpatient reception request: id={}", id);
        InpatientReceptionDTO dto = inpatientReceptionService.getInpatientReception(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inpatient reception fetched", dto));
    }

    @Operation(summary = "Create inpatient reception")
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> createInpatientReception(@Valid @RequestBody InpatientReceptionDTO request) {
        log.info("Create inpatient reception request: receptionNo={}", request.getReceptionNo());
        inpatientReceptionService.createInpatientReception(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inpatient reception created", true));
    }

    @Operation(summary = "Update inpatient reception")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateInpatientReception(
            @Parameter(description = "Reception id") @PathVariable Long id,
            @Valid @RequestBody InpatientReceptionDTO request
    ) {
        log.info("Update inpatient reception request: id={}", id);
        inpatientReceptionService.updateInpatientReception(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inpatient reception updated", true));
    }

    @Operation(summary = "Cancel inpatient reception")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> cancelInpatientReception(
            @Parameter(description = "Reception id") @PathVariable Long id
    ) {
        log.info("Cancel inpatient reception request: id={}", id);
        InpatientReceptionDTO request = new InpatientReceptionDTO();
        request.setStatus("CANCELLED");
        request.setIsActive(false);
        request.setNote("Cancelled from frontend");
        inpatientReceptionService.updateInpatientReception(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inpatient reception cancelled", true));
    }
}
