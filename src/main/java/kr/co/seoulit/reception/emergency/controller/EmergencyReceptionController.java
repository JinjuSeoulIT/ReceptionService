package kr.co.seoulit.reception.emergency.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.seoulit.common.api.ApiResponse;
import kr.co.seoulit.reception.emergency.dto.EmergencyReceptionDTO;
import kr.co.seoulit.reception.emergency.service.EmergencyReceptionService;
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
@RequestMapping("/api/emergency-receptions")
@Tag(name = "Emergency Reception", description = "Emergency reception APIs")
@Slf4j
@Validated
public class EmergencyReceptionController {

    private final EmergencyReceptionService emergencyReceptionService;

    @Operation(summary = "Get emergency receptions")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmergencyReceptionDTO>>> getEmergencyReceptions(
            @Parameter(description = "Search type") @RequestParam(required = false) String searchType,
            @Parameter(description = "Search value") @RequestParam(required = false) String searchValue
    ) {
        log.info("Get emergency receptions request: searchType={}, searchValue={}", searchType, searchValue);
        HashMap<String, Object> searchCondition = new HashMap<>();
        searchCondition.put("searchType", searchType);
        searchCondition.put("searchValue", searchValue);

        List<EmergencyReceptionDTO> list = emergencyReceptionService.getEmergencyReceptionList(searchCondition);
        return ResponseEntity.ok(new ApiResponse<>(true, "Emergency reception list fetched", list));
    }

    @Operation(summary = "Get emergency reception detail")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmergencyReceptionDTO>> getEmergencyReception(
            @Parameter(description = "Reception id") @PathVariable Long id
    ) {
        log.info("Get emergency reception request: id={}", id);
        EmergencyReceptionDTO dto = emergencyReceptionService.getEmergencyReception(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Emergency reception fetched", dto));
    }

    @Operation(summary = "Create emergency reception")
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> createEmergencyReception(@Valid @RequestBody EmergencyReceptionDTO request) {
        log.info("Create emergency reception request: receptionNo={}", request.getReceptionNo());
        emergencyReceptionService.createEmergencyReception(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Emergency reception created", true));
    }

    @Operation(summary = "Update emergency reception")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateEmergencyReception(
            @Parameter(description = "Reception id") @PathVariable Long id,
            @Valid @RequestBody EmergencyReceptionDTO request
    ) {
        log.info("Update emergency reception request: id={}", id);
        emergencyReceptionService.updateEmergencyReception(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Emergency reception updated", true));
    }

    @Operation(summary = "Cancel emergency reception")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> cancelEmergencyReception(
            @Parameter(description = "Reception id") @PathVariable Long id
    ) {
        log.info("Cancel emergency reception request: id={}", id);
        EmergencyReceptionDTO request = new EmergencyReceptionDTO();
        request.setStatus("CANCELLED");
        request.setIsActive(false);
        request.setNote("Cancelled from frontend");
        emergencyReceptionService.updateEmergencyReception(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Emergency reception cancelled", true));
    }
}
