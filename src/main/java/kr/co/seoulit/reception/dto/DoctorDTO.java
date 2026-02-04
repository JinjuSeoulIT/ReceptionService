package kr.co.seoulit.reception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Doctor lookup")
@Data
public class DoctorDTO {
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
}
