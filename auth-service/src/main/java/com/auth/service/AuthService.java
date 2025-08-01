package com.auth.service;

import com.auth.config.JwtProperties;
import com.auth.exception.CustomJwtException;
import com.auth.exception.CustomLoginException;
import com.auth.exception.CustomSignupException;
import com.auth.model.entity.UserEntity;
import com.auth.model.request.LoginRequest;
import com.auth.model.request.SignupRequest;
import com.auth.model.response.LoginResponse;
import com.auth.model.response.RefreshTokenResponse;
import com.auth.model.response.SignupResponse;
import com.auth.model.response.UserResponse;
import com.auth.model.role.UserRole;
import com.auth.repository.UserRepository;
import com.auth.security.CustomUserDetails;
import com.auth.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final long accessTokenExpiry;

    private final long refreshTokenExpiry;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessTokenExpiry = jwtProperties.getAccessTokenExpiry();
        this.refreshTokenExpiry = jwtProperties.getRefreshTokenExpiry();
    }

    /**
     * 사용자명과 이메일 중복을 확인하고, 비밀번호를 암호화하여 새 사용자를 생성합니다.
     * 
     * @param request 회원가입 요청 정보
     * @return 회원가입 성공 응답
     * @throws CustomSignupException 사용자명 또는 이메일이 이미 존재하는 경우
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            log.warn("Username {} already exists", request.username());
            throw new CustomSignupException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Email {} already exists", request.email());
            throw new CustomSignupException(HttpStatus.CONFLICT, "Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        String username = request.username();
        String email = request.email();
        UserRole userRole = UserRole.ROLE_USER;
        UserEntity userEntity = new UserEntity(username, encodedPassword, email, userRole);

        userRepository.save(userEntity);
        log.info("User signed up: {}", request.username());

        return new SignupResponse(username, email);
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
        String username = loginRequest.username();
        String password = loginRequest.password();
        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        try {
            // CustomUserDetailsService.loadUserByUsername() 실행함
            authentication = authenticationManager.authenticate(authentication);
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user {}: ", loginRequest.username(), e);
            throw new CustomLoginException(HttpStatus.UNAUTHORIZED, "username or password is incorrect");
        }
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = customUserDetails.getUserEntity().getUserId();
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
