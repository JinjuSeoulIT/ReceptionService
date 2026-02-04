package kr.co.seoulit.reception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Department lookup")
@Data
public class DepartmentDTO {
    private Long departmentId;
    private String departmentName;
}
