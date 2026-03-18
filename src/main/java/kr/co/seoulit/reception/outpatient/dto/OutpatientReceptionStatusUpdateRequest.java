package kr.co.seoulit.reception.outpatient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OutpatientReceptionStatusUpdateRequest {
    @NotBlank(message = "status is required")
    private String status;
    private Long changedBy;
    private String reasonCode;
    private String reasonText;
}



