package com.lloyds.onboard.exception;

import com.lloyds.onboard.config.ErrorConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Map<String, ErrorDetails> errorDetailsMap;

    private final ErrorDetails defualtErrorDetails = ErrorDetails.builder()
            .errorMessage("An unexpected error occurred")
            .errorType("UnknownError")
            .statusCode(500)
            .errorCode("9999")
            .build();

    public GlobalExceptionHandler(ErrorConfig errorConfig) {
        this.errorDetailsMap = errorConfig.getErrorDetails().stream()
                .collect(Collectors.toMap(ErrorDetails::getErrorCode, errorDetails -> errorDetails));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorDetails> handleServiceException(ServiceException ex) {
        ErrorDetails errorDetails = errorDetailsMap.getOrDefault(ex.getErrorCode(), defualtErrorDetails);
        return ResponseEntity.status(errorDetails.getStatusCode()).body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGenericException(Exception ex) {
        return ResponseEntity.status(defualtErrorDetails.getStatusCode()).body(defualtErrorDetails);
    }
}