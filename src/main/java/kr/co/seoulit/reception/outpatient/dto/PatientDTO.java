package kr.co.seoulit.reception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "환자 조회")
@Data
public class PatientDTO {
    private Long patientId;
    private String patientName;
}
