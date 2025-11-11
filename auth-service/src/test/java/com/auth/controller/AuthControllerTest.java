package com.auth.controller;

import com.auth.model.request.LoginRequest;
import com.auth.model.request.SignupRequest;
import com.auth.model.response.LoginResponse;
import com.auth.model.response.RefreshTokenResponse;
import com.auth.model.response.SignupResponse;
import com.auth.model.response.UserResponse;
import com.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController 단위 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Nested
    @DisplayName("POST /auth/v1/signup")
    class SignupTest {
        @Test
        @DisplayName("회원가입 요청이 유효하면 200 응답과 사용자 정보 반환")
        void givenValidSignupRequest_whenSignup_thenReturnsSignupResponse() throws Exception {
            // Given
            SignupRequest signupRequest = new SignupRequest("tester@email.com", "password123", "tester");
            SignupResponse signupResponse = new SignupResponse("tester@email.com", "tester");

            given(authService.signup(any())).willReturn(signupResponse);

            // When
            ResultActions result = mockMvc.perform(post("/auth/v1/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest)));

            // Then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("tester"))
                    .andExpect(jsonPath("$.email").value("tester@email.com"));

            then(authService).should().signup(any());
        }

        @Test
        @DisplayName("회원가입 요청이 유효하지 않으면 400 반환")
        void givenInvalidSignupRequest_whenSignup_thenReturns400() throws Exception {
            // Given
            SignupRequest signupRequest = new SignupRequest("", "short", "invalid-email");

            // When
            ResultActions resultActions = mockMvc.perform(post("/auth/v1/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest)));

            // Then
            resultActions.andDo(print())
                    .andExpect(status().isBadRequest());

            then(authService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("POST /auth/v1/login")
    class LoginTest {
        @Test
        @DisplayName("로그인 요청이 유효하면 200 응답과 토큰 반환")
        void givenValidLoginRequest_whenLogin_thenReturnsLoginResponse() throws Exception {
            // Given
            LoginRequest loginRequest = new LoginRequest("tester", "password123");
            LoginResponse loginResponse = new LoginResponse(
                    "access-token", "refresh-token", 3600L, 7200L,
                    new UserResponse(1L, "tester", "ROLE_USER")
            );

            given(authService.login(any(), any())).willReturn(loginResponse);

            // When
            ResultActions resultActions = mockMvc.perform(post("/auth/v1/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)));

            // Then
            resultActions.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.user.name").value("tester"));

            then(authService).should().login(any(), eq(loginRequest));
        }
    }

    @Nested
    @DisplayName("POST /auth/v1/refresh")
    class RefreshTokenTest {
        @Test
        @DisplayName("유효한 리프레시 토큰이 주어지면 액세스 토큰을 갱신한다")
        void givenValidRefreshToken_whenRefresh_thenReturnsAccessToken() throws Exception {
            // Given
            String refreshToken = "refresh-token";
            RefreshTokenResponse refreshTokenResponse = new RefreshTokenResponse(
                    "new-access-token", 3600L, new UserResponse(1L, "tester", "ROLE_USER")
            );

            given(authService.refreshAccessToken(refreshToken)).willReturn(refreshTokenResponse);

            // When
            ResultActions resultActions = mockMvc.perform(post("/auth/v1/refresh")
                    .header("x-refresh-token", refreshToken));

            // Then
            resultActions.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                    .andExpect(jsonPath("$.user.name").value("tester"));

            then(authService).should().refreshAccessToken(refreshToken);
        }

        @Test
        @DisplayName("리프레시 토큰이 없으면 500 반환")
        void givenNoRefreshToken_whenRefresh_thenReturns500() throws Exception {
            // When
            ResultActions resultActions = mockMvc.perform(post("/auth/v1/refresh"));

            // Then
            resultActions.andDo(print())
                    .andExpect(status().isInternalServerError());

            then(authService).shouldHaveNoInteractions();
        }
    }

}
