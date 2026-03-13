package kr.co.seoulit.common.controller;

import kr.co.seoulit.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public ApiResponse<String> root() {
<<<<<<< HEAD:src/main/java/kr/co/seoulit/common/controller/RootController.java
        return new ApiResponse<>(true, "OK", "reception-backend");
=======
        return new ApiResponse<>(true, "OK", "Reception API is running");
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/common/controller/RootController.java
    }
}
