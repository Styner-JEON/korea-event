package com.event.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomEventException.class)
    public ResponseEntity<ErrorResponse> handleCustomEventException(CustomEventException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(CustomJwtException.class)
    public ResponseEntity<ErrorResponse> handleCustomJwtException(CustomJwtException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(CustomCommentException.class)
    public ResponseEntity<ErrorResponse> handleCustomCommentException(CustomCommentException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Validation ERROR: ", e);
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .collect(Collectors.joining("\n"));
        ErrorResponse response = new ErrorResponse(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        String message = "Unexpected exception occurred!";
        log.error(message, e);
        ErrorResponse response = new ErrorResponse(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
