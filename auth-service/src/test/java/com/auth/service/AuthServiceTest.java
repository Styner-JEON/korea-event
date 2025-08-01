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
import com.auth.model.role.UserRole;
import com.auth.repository.UserRepository;
import com.auth.security.CustomUserDetails;
import com.auth.security.JwtUtil;
import com.auth.security.PasswordEncoderConfig;
import io.jsonwebtoken.io.Encoders;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletResponse httpServletResponse;

    private JwtUtil jwtUtil;

    private PasswordEncoder passwordEncoder;

    private final long accessTokenExpiry = 1000L * 60 * 30;           // 1000 * 60 * 30 = 30분

    private final long refreshTokenExpiry = 1000L * 60 * 60 * 24 * 7; // 1000 * 60 * 60 * 24 * 7 = 7일

    @BeforeEach
    void setUp() {
        String rawKey = "StynerIsTestingJwtSecureKey!!_32";
        String base64UrlEncodedKey = Encoders.BASE64URL.encode(rawKey.getBytes(StandardCharsets.UTF_8));
        JwtProperties jwtProperties = new JwtProperties(base64UrlEncodedKey, accessTokenExpiry, refreshTokenExpiry);

        jwtUtil = new JwtUtil(jwtProperties);
        passwordEncoder = new PasswordEncoderConfig().passwordEncoder();
        authService = new AuthService(authenticationManager, jwtUtil, userRepository, passwordEncoder, jwtProperties);
    }

    @Nested
    @DisplayName("회원가입")
    class SignupTest {
        @Test
        @DisplayName("정상적으로 회원가입 시 SignupResponse 반환")
        void givenValidRequest_whenSignup_thenReturnsResponse() {
            // Given
            SignupRequest signupRequest = new SignupRequest("tester", "password123", "tester@email.com");
            UserEntity userEntity = new UserEntity("tester", passwordEncoder.encode("password123"), "tester@email.com", UserRole.ROLE_USER);

            given(userRepository.existsByUsername("tester")).willReturn(false);
            given(userRepository.existsByEmail("tester@email.com")).willReturn(false);
            given(userRepository.save(any())).willReturn(userEntity);

            // When
            SignupResponse signupResponse = authService.signup(signupRequest);

            // Then
            assertThat(signupResponse.username()).isEqualTo("tester");
            assertThat(signupResponse.email()).isEqualTo("tester@email.com");

            then(userRepository).should().save(any());
        }

        @Test
        @DisplayName("이미 존재하는 사용자명일 경우 예외 발생")
        void givenDuplicateUsername_whenSignup_thenThrowsException() {
            // Given
            SignupRequest signupRequest = new SignupRequest("tester", "password123", "new@email.com");

            given(userRepository.existsByUsername("tester")).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.signup(signupRequest)).isInstanceOf(CustomSignupException.class);

            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 존재하는 이메일일 경우 예외 발생")
        void givenDuplicateEmail_whenSignup_thenThrowsException() {
            // Given
            SignupRequest signupRequest = new SignupRequest("tester", "password123", "dup@email.com");

            given(userRepository.existsByUsername("tester")).willReturn(false);
            given(userRepository.existsByEmail("dup@email.com")).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.signup(signupRequest)).isInstanceOf(CustomSignupException.class);

            then(userRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("로그인")
    class LoginTest {
        @Test
        @DisplayName("정상적으로 로그인 시 LoginResponse 반환")
        void givenValidLogin_whenLogin_thenReturnsResponse() {
            // Given
            LoginRequest loginRequest = new LoginRequest("tester", "password123");
            Authentication unauthenticated = new UsernamePasswordAuthenticationToken("tester", "password123");

            UserEntity userEntity = new UserEntity("tester", "encodedPassword", "tester@email.com", UserRole.ROLE_USER);
            ReflectionTestUtils.setField(userEntity, "userId", 1L);
            CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);
            Authentication authenticated = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    null,
                    customUserDetails.getAuthorities()
            );

            given(authenticationManager.authenticate(unauthenticated)).willReturn(authenticated);

            // When
            LoginResponse loginResponse = authService.login(httpServletResponse, loginRequest);

            // Then
            assertThat(loginResponse.accessToken()).isNotBlank();
            assertThat(loginResponse.refreshToken()).isNotBlank();
            assertThat(loginResponse.user().name()).isEqualTo("tester");
        }

        @Test
        @DisplayName("잘못된 인증 정보일 경우 예외 발생")
        void givenInvalidCredentials_whenLogin_thenThrowsException() {
            // Given
            LoginRequest loginRequest = new LoginRequest("tester", "wrongPassword");
            Authentication unauthenticated = new UsernamePasswordAuthenticationToken("tester", "wrongPassword");

            given(authenticationManager.authenticate(unauthenticated)).willThrow(new BadCredentialsException("bad"));

            // When & Then
            assertThatThrownBy(() -> authService.login(httpServletResponse, loginRequest))
                    .isInstanceOf(CustomLoginException.class);
        }
    }

    @Nested
    @DisplayName("토큰 재발급")
    class RefreshTokenTest {
        @Test
        @DisplayName("유효한 refreshToken으로 accessToken 재발급")
        void givenValidRefreshToken_whenRefresh_thenReturnsAccessToken() {
            // Given
            String refreshToken = jwtUtil.createJwt(1L, "tester", "ROLE_USER", refreshTokenExpiry);

            // When
            RefreshTokenResponse refreshTokenResponse = authService.refreshAccessToken(refreshToken);

            // Then
            assertThat(refreshTokenResponse.accessToken()).isNotBlank();
            assertThat(refreshTokenResponse.user().name()).isEqualTo("tester");
        }

        @Test
        @DisplayName("refreshToken이 null이면 예외 발생")
        void givenNullRefreshToken_whenRefresh_thenThrowsException() {
            // Given

            // When & Then
            assertThatThrownBy(() -> authService.refreshAccessToken(null))
                    .isInstanceOf(CustomJwtException.class);
        }
    }

}
