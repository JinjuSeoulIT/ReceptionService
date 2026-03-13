// src/main/java/kr/co/seoulit/reception/exception/ErrorResponse.java
package kr.co.seoulit.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String code;
    private String message;
}

