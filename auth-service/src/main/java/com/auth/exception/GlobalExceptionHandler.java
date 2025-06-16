package com.auth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomSignupException.class)
    public ResponseEntity<ErrorResponse> handleSignupException(CustomSignupException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(CustomLoginException.class)
    public ResponseEntity<ErrorResponse> handleLoginException(CustomLoginException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        String message = "Unexpected exception occurred!";
        log.error(message, e);
        ErrorResponse response = new ErrorResponse(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
