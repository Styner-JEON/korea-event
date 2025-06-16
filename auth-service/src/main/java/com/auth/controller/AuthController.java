package com.auth.controller;

import com.auth.model.request.LoginRequest;
import com.auth.model.request.RefreshTokenRequest;
import com.auth.model.request.SignupRequest;
import com.auth.model.response.LoginResponse;
import com.auth.model.response.RefreshTokenResponse;
import com.auth.model.response.SignupResponse;
import com.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/auth/${api.version}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(HttpServletResponse response, @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(response, loginRequest);
        log.info("Login successful: {}", loginRequest.username());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<RefreshTokenResponse> refreshAccessToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authService.refreshAccessToken(refreshTokenRequest));
    }

}
