package com.kiseok.pingmall.web.dto;

import com.kiseok.pingmall.common.errors.ErrorCode;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ErrorResponseDto {

    private String status;
    private String message;
    private List<ExtractFieldError> errors = new ArrayList<>();
    private String code;
    private LocalDateTime erroredAt;

    public static ErrorResponseDto createErrorResponseDto(ErrorCode errorCode, List<ExtractFieldError> errors) {
        return ErrorResponseDto.builder()
                .status(errorCode.getStatus())
                .message(errorCode.getMessage())
                .errors(errors)
                .code(errorCode.getCode())
                .erroredAt(LocalDateTime.now())
                .build();
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class ExtractFieldError   {
        private String field;
        private String value;
        private String reason;
    }
}
