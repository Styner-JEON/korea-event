package com.auth.exception;

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
 * 애플리케이션에서 발생하는 모든 예외를 중앙에서 처리합니다.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 회원가입 관련 예외 처리
     * 
     * @param e 회원가입 예외
     * @return 에러 응답
     */
    @ExceptionHandler(CustomSignupException.class)
    public ResponseEntity<ErrorResponse> handleSignupException(CustomSignupException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    /**
     * 로그인 관련 예외 처리
     * 
     * @param e 로그인 예외
     * @return 에러 응답
     */
    @ExceptionHandler(CustomLoginException.class)
    public ResponseEntity<ErrorResponse> handleLoginException(CustomLoginException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    /**
     * 유효성 검증 실패 예외 처리
     * 요청 데이터의 유효성 검증이 실패했을 때 발생하는 예외를 처리합니다.
     * 
     * @param e 유효성 검증 예외
     * @return 에러 응답 (검증 실패 필드들의 메시지 포함)
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
     * 이메일 관련 예외 처리
     *
     * @param e 이메일 예외
     * @return 에러 응답
     */
    @ExceptionHandler(CustomEmailException.class)
    public ResponseEntity<ErrorResponse> handleEmailException(CustomEmailException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    /**
     * 일반적인 예외 처리
     * 위에서 처리되지 않은 모든 예외를 처리합니다.
     * 
     * @param e 일반 예외
     * @return 서버 내부 오류 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        String message = "Unexpected exception occurred!";
        log.error(message, e);
        ErrorResponse response = new ErrorResponse(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
