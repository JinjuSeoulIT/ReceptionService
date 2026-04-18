package kr.co.seoulit.reception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "의사 조회")
@Data
public class DoctorDTO {
    private String doctorId;
    private String doctorName;
    private String departmentId;
}
