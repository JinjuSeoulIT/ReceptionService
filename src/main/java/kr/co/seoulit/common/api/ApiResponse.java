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

<<<<<<< HEAD:src/main/java/kr/co/seoulit/common/api/ApiResponse.java
    @Schema(description = "값")
=======
    @Schema(description = "데이터")
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/common/api/ApiResponse.java
    private T result;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data);
    }

<<<<<<< HEAD:src/main/java/kr/co/seoulit/common/api/ApiResponse.java
    // 실패 응답 (업무 상태)
=======
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/common/api/ApiResponse.java
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
<<<<<<< HEAD:src/main/java/kr/co/seoulit/common/api/ApiResponse.java

=======
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/common/api/ApiResponse.java
