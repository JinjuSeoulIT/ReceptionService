package kr.co.seoulit.reception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "진료과 조회")
@Data
public class DepartmentDTO {
    private String departmentId;
    private String departmentName;
}
