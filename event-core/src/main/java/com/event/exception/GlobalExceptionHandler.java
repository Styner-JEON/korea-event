package com.event.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 * 
 * 애플리케이션에서 발생하는 모든 예외를 일관된 형태로 처리하여
 * 클라이언트에게 적절한 오류 응답을 제공합니다.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 이벤트 관련 커스텀 예외를 처리합니다.
     * 
     * @param e 발생한 이벤트 관련 예외
     * @return 오류 응답
     */
    @ExceptionHandler(CustomEventException.class)
    public ResponseEntity<ErrorResponse> handleCustomEventException(CustomEventException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    /**
     * JWT 관련 커스텀 예외를 처리합니다.
     * 
     * @param e 발생한 JWT 관련 예외
     * @return 오류 응답
     */
    @ExceptionHandler(CustomJwtException.class)
    public ResponseEntity<ErrorResponse> handleCustomJwtException(CustomJwtException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    /**
     * 댓글 관련 커스텀 예외를 처리합니다.
     * 
     * @param e 발생한 댓글 관련 예외
     * @return 오류 응답
     */
    @ExceptionHandler(CustomCommentException.class)
    public ResponseEntity<ErrorResponse> handleCustomCommentException(CustomCommentException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    /**
     * 요청 데이터 검증 실패 예외를 처리합니다.
     * 
     * Bean Validation을 통한 요청 검증이 실패했을 때
     * 모든 검증 오류 메시지를 결합하여 응답합니다.
     * 
     * @param e 발생한 검증 실패 예외
     * @return 검증 오류 메시지가 포함된 오류 응답
     */
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

    /**
     * AI 관련 커스텀 예외를 처리합니다.
     * 
     * @param e 발생한 AI 관련 예외
     * @return 오류 응답
     */
    @ExceptionHandler(CustomAiException.class)
    public ResponseEntity<ErrorResponse> handleCustomAiException(CustomAiException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    /**
     * 예상하지 못한 모든 예외를 처리합니다.
     * 
     * 시스템에서 처리되지 않은 예외가 발생했을 때
     * 일반적인 오류 메시지와 함께 500 상태코드를 반환합니다.
     * 
     * @param e 발생한 예외
     * @return 일반적인 오류 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        String message = "Unexpected exception occurred!";
        log.error(message, e);
        ErrorResponse response = new ErrorResponse(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
