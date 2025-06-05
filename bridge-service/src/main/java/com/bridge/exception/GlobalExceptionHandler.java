package com.bridge.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

//    @ExceptionHandler(PublicDataApiClientException.class)
//    public ResponseEntity<String> handlePublicDataApiException(PublicDataApiClientException e) {
//        log.error("PublicDataApiClient error", e);
//        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
//    }
//
//    @ExceptionHandler(KafkaSendingException.class)
//    public ResponseEntity<String> handleKafkaSendException(KafkaSendingException e) {
//        log.error("Kafka sending error", e);
//        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
//    }
//
//    @ExceptionHandler(BatchProcessingException.class)
//    public ResponseEntity<String> handleBatchProcessingException(BatchProcessingException e) {
//        log.error("Batch processing error", e);
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handleGenericException(Exception e) {
//        log.error("Unexpected error occurred!", e);
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred!");
//    }

    @ExceptionHandler(CustomPublicDataApiException.class)
    public ResponseEntity<ErrorResponse> handleCustomPublicDataApiException(CustomPublicDataApiException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(CustomKafkaException.class)
    public ResponseEntity<ErrorResponse> handleCustomKafkaException(CustomKafkaException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(CustomBatchException.class)
    public ResponseEntity<ErrorResponse> handleCustomBatchException(CustomBatchException e) {
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
