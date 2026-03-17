package kr.co.seoulit.common.controller;

import kr.co.seoulit.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public ApiResponse<String> root() {
        return new ApiResponse<>(true, "OK", "Reception API is running");
    }
}
