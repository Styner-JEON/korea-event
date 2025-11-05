package com.auth.controller;

import com.auth.model.request.LoginRequest;
import com.auth.model.request.SignupRequest;
import com.auth.model.response.LoginResponse;
import com.auth.model.response.RefreshTokenResponse;
import com.auth.model.response.SignupResponse;
import com.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러
 *
 * 사용자 회원가입, 로그인, 토큰 갱신 기능을 제공합니다.
 */
@RestController
@RequestMapping(path = "/auth/${api.version}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 사용자 회원가입
     * 
     * @param signupRequest 회원가입 요청 정보 (이메일, 사용자명, 비밀번호)
     * @return 회원가입 성공 응답 (이메일, 사용자명)
     */
    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(authService.signup(signupRequest));
    }

    /**
     * 사용자 로그인
     * 
     * @param response     HTTP 응답 객체
     * @param loginRequest 로그인 요청 정보 (이메일, 비밀번호)
     * @return 로그인 성공 응답 (액세스 토큰, 리프레시 토큰, 사용자 정보)
     */
    @PostMapping("/login")
    @Operation(summary = "로그인")
    public ResponseEntity<LoginResponse> login(HttpServletResponse response, @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(response, loginRequest);
        log.info("Login successful: {}", loginRequest.email());
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * 액세스 토큰 갱신
     * 
     * @param refreshToken 리프레시 토큰 (헤더에서 전달)
     * @return 새로운 액세스 토큰과 사용자 정보
     */
    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 갱신")
    public ResponseEntity<RefreshTokenResponse> refreshAccessToken(
            @RequestHeader("x-refresh-token") String refreshToken) {
        return ResponseEntity.ok(authService.refreshAccessToken(refreshToken));
    }

}
