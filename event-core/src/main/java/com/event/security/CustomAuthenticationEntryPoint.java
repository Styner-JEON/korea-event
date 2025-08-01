package com.event.security;

import com.event.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 커스텀 인증 엔트리 포인트 클래스
 * 
 * Spring Security에서 인증 실패 시 호출되는 핸들러입니다.
 * 인증되지 않은 사용자가 보호된 리소스에 접근할 때 JSON 형태의 에러 응답을 반환합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * 인증 실패 시 호출되는 메서드
     * 
     * 인증되지 않은 요청에 대해 HTTP 401 Unauthorized 상태코드와
     * JSON 형태의 에러 메시지를 응답으로 반환합니다.
     * 
     * @param request                 인증 실패한 HTTP 요청
     * @param response                HTTP 응답 객체
     * @param authenticationException 인증 실패 예외 객체
     * @throws IOException      IO 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException) throws IOException, ServletException {
        log.warn("Authentication failed: {} | URL: {}", authenticationException.getMessage(), request.getRequestURI(),
                authenticationException);

        // 에러 응답 객체 생성
        ErrorResponse errorResponse = new ErrorResponse(authenticationException.getMessage());

        // HTTP 응답 설정
        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401 상태코드 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // Content-Type을 JSON으로 설정
        response.setCharacterEncoding("UTF-8"); // 문자 인코딩을 UTF-8로 설정

        // JSON 형태의 에러 응답을 클라이언트에 전송
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(errorResponse));
        writer.flush();
    }

}
