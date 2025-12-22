package com.auth.service;

import com.auth.exception.CustomSignupException;
import com.auth.model.entity.UserEntity;
import com.auth.model.request.SignupRequest;
import com.auth.model.response.*;
import com.auth.model.role.UserRole;
import com.auth.properties.JwtProperties;
import com.auth.exception.CustomJwtException;
import com.auth.exception.CustomLoginException;
import com.auth.model.request.LoginRequest;
import com.auth.repository.UserRepository;
import com.auth.security.CustomUserDetails;
import com.auth.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스 클래스
 *
 * 사용자 회원가입, 로그인, 토큰 갱신 등의 인증 관련 비즈니스 로직을 처리합니다.
 */
@Service
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final long accessTokenExpiry;

    private final long refreshTokenExpiry;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            JwtProperties jwtProperties
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.accessTokenExpiry = jwtProperties.getAccessTokenExpiry();
        this.refreshTokenExpiry = jwtProperties.getRefreshTokenExpiry();
    }

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {
        String email = signupRequest.email().toLowerCase();
        String username = signupRequest.username();

        if (userRepository.existsByEmail(email)) {
            log.warn("Email {} already exists", email);
            throw new CustomSignupException(HttpStatus.CONFLICT, "Email already exists");
        }

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            log.warn("Username {} already exists", username);
            throw new CustomSignupException(HttpStatus.CONFLICT, "Username already exists");
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.password());
        UserRole userRole = UserRole.ROLE_USER;

        UserEntity userEntity = new UserEntity(email, username, encodedPassword, userRole);
        userRepository.save(userEntity);
        log.info("User signed up: {}", email);

        return new SignupResponse(email, username);
    }

    /**
     * 사용자 인증을 수행하고 성공 시 액세스 토큰과 리프레시 토큰을 발급합니다.
     * 
     * @param response     HTTP 응답 객체
     * @param loginRequest 로그인 요청 정보
     * @return 로그인 성공 응답 (토큰 및 사용자 정보 포함)
     * @throws CustomLoginException 인증 실패 시
     */
    public LoginResponse login(HttpServletResponse response, LoginRequest loginRequest) {
        String email = loginRequest.email().toLowerCase();
        String password = loginRequest.password();
        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(email, password);
        try {
            // CustomUserDetailsService.loadUserByUsername() 실행함
            authentication = authenticationManager.authenticate(authentication);
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user {}: ", email, e);
            throw new CustomLoginException(HttpStatus.UNAUTHORIZED, "Incorrect email or password");
        }
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = customUserDetails.getUserEntity().getUserId();
        String username = authentication.getName();
        String userRole = customUserDetails.getUserEntity().getUserRole().name();

        String accessToken = jwtUtil.createJwt(userId, username, userRole, accessTokenExpiry);
        String refreshToken = jwtUtil.createJwt(userId, username, userRole, refreshTokenExpiry);
        return new LoginResponse(
                accessToken,
                refreshToken,
                accessTokenExpiry,
                refreshTokenExpiry,
                new UserResponse(userId, username, userRole));
    }

    /**
     * 리프레시 토큰을 검증하고 새로운 액세스 토큰을 발급합니다.
     * 
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰과 사용자 정보
     * @throws CustomJwtException 리프레시 토큰이 없거나 유효하지 않은 경우
     */
    public RefreshTokenResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null) {
            log.warn("No refreshToken");
            throw new CustomJwtException(HttpStatus.UNAUTHORIZED, "NO_REFRESH_TOKEN");
        }

        Jws<Claims> jwsClaims = jwtUtil.readJwt(refreshToken);
        Claims claims = jwsClaims.getPayload();

        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        String userRole = claims.get("userRole", String.class);
        String accessToken = jwtUtil.createJwt(userId, username, userRole, accessTokenExpiry);

        return new RefreshTokenResponse(accessToken, accessTokenExpiry, new UserResponse(userId, username, userRole));
    }

}
