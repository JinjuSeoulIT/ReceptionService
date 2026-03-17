package kr.co.seoulit.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class ApiResponse<T> {
    @Schema(description = "성공여부")
    private boolean success;

    @Schema(description = "응답 메시지")
    private String message;

    @Schema(description = "데이터")
    private T result;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
