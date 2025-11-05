package com.auth.integration;

import com.auth.model.entity.UserEntity;
import com.auth.model.request.LoginRequest;
import com.auth.model.request.SignupRequest;
import com.auth.model.role.UserRole;
import com.auth.repository.UserRepository;
import com.auth.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("인증 통합 테스트")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String API_VERSION = "v1";

    private static final String USERNAME = "testuser";

    private static final String PASSWORD = "testpass123";

    private static final String EMAIL = "test@email.com";

    private String jwt;

    @BeforeEach
    void setUp() throws Exception {
        jwt = generateJwt(1L, USERNAME, "ROLE_USER");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("회원가입")
    class SignupTest {
        @Test
        @DisplayName("유효한 요청이 주어지면 회원가입에 성공한다")
        void givenValidRequest_whenSignup_thenReturns200() throws Exception {
            // Given
            SignupRequest signupRequest = new SignupRequest(USERNAME, PASSWORD, EMAIL);

            // When
            ResultActions resultActions = mockMvc.perform(post("/auth/" + API_VERSION + "/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest)));

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.email").value(EMAIL));

            assertThat(userRepository.existsByUsernameIgnoreCase(USERNAME)).isTrue();
        }

        @Test
        @DisplayName("중복된 사용자명일 경우 409")
        void givenDuplicateUsername_whenSignup_thenReturns409() throws Exception {
            // Given
            SignupRequest signupRequest = new SignupRequest(USERNAME, PASSWORD, EMAIL);

            UserEntity userEntity = new UserEntity(USERNAME, PASSWORD, "other@email.com", UserRole.ROLE_USER);
            userRepository.save(userEntity);

            // When
            ResultActions resultActions = mockMvc.perform(post("/auth/" + API_VERSION + "/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest)));

            // Then
            resultActions.andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("로그인")
    class LoginTest {
        @Test
        @DisplayName("올바른 자격증명이 주어졌을 때, 로그인에 성공하고 토큰이 발급된다")
        void givenValidCredentials_whenLogin_thenReturnsTokens() throws Exception {
            // Given
            LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD);

            UserEntity userEntity = new UserEntity(USERNAME, passwordEncoder.encode(PASSWORD), EMAIL, UserRole.ROLE_USER);
            userRepository.save(userEntity);

            // When
            ResultActions resultActions = mockMvc.perform(post("/auth/" + API_VERSION + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)));

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.user.name").value(USERNAME));
        }

        @Test
        @DisplayName("잘못된 비밀번호일 경우 401")
        void givenWrongPassword_whenLogin_thenReturns401() throws Exception {
            // Given
            LoginRequest loginRequest = new LoginRequest(USERNAME, "wrongpass");

            UserEntity userEntity = new UserEntity(USERNAME, passwordEncoder.encode(PASSWORD), EMAIL, UserRole.ROLE_USER);
            userRepository.save(userEntity);

            // When
            ResultActions resultActions = mockMvc.perform(post("/auth/" + API_VERSION + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)));

            // Then
            resultActions.andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    class RefreshTokenTest {
        @Test
        @DisplayName("유효한 리프레시 토큰을 제공하면 새로운 액세스 토큰을 발급한다")
        void givenValidRefreshToken_whenRefresh_thenReturnsNewAccessToken() throws Exception {
            // Given
            String refreshToken = generateJwt(1L, USERNAME, "ROLE_USER");

            // When
            ResultActions result = mockMvc.perform(post("/auth/" + API_VERSION + "/refresh")
                    .header("x-refresh-token", refreshToken));

            // Then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.user.name").value(USERNAME));
        }

        @Test
        @DisplayName("토큰 없이 요청하면 500 (MissingRequestHeaderException)")
        void givenNoToken_whenRefresh_thenReturns500() throws Exception {
            // Given

            // When
            ResultActions result = mockMvc.perform(post("/auth/" + API_VERSION + "/refresh"));

            // Then
            result.andExpect(status().isInternalServerError());
        }
    }

    private String generateJwt(Long userId, String username, String role) throws Exception {
        Field field = jwtUtil.getClass().getDeclaredField("secretKey");
        field.setAccessible(true);
        SecretKey secretKey = (SecretKey) field.get(jwtUtil);

        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("userRole", role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + 10 * 60 * 1000))
                .signWith(secretKey)
                .compact();
    }

}
