package com.event.integration;

import com.event.model.entity.CommentEntity;
import com.event.repository.CommentRepository;
import com.event.security.JwtUtil;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AI 댓글 분석 통합 테스트")
class AiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Long TEST_USER_ID = 200L;

    private static final String TEST_USERNAME = "aiuser";

    private static final String TEST_ROLE = "ROLE_USER";

    private String jwt;

    @Value("${size.required-ai-comment}")
    private int requiredAiCommentSize;

    @BeforeEach
    void setUp() throws Exception {
        jwt = generateJwt();
    }

    @AfterEach
    void tearDown() {
        commentRepository.deleteAll();
    }

    @Nested
    @DisplayName("댓글 AI 요약 요청")
    class AnalyzeCommentsTest {
        @Test
        @DisplayName("댓글이 limit개 이상일 때, AI 분석 결과가 정상적으로 반환된다")
        void givenLimitComments_whenGetSummary_thenReturnsAnalysis() throws Exception {
            // Given
            for (int i = 1; i <= requiredAiCommentSize; i++) {
                commentRepository.save(createCommentEntity("AI 댓글 " + i));
            }

            // When
            ResultActions resultActions = mockMvc.perform(get("/ai/v1/1/analysis")
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$.summary").isString())
                    .andExpect(jsonPath("$.keywords").isArray())
                    .andExpect(jsonPath("$.emotion.overall").isString())
                    .andExpect(jsonPath("$.emotion.ratio.positive").isNumber())
                    .andExpect(jsonPath("$.emotion.mainEmotions").isArray());
        }

        @Test
        @DisplayName("댓글이 limit개 미만일 경우 400 반환한다")
        void givenLessThanLimitComments_whenGetSummary_thenReturns400() throws Exception {
            // Given
            for (int i = 1; i < requiredAiCommentSize; i++) {
                commentRepository.save(createCommentEntity("댓글 " + i));
            }

            // When
            ResultActions resultActions = mockMvc.perform(get("/ai/v1/1/analysis")
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions.andExpect(status().isBadRequest());
        }
    }

    /**
     * 테스트용 JWT를 직접 생성하는 메서드.
     * <p>
     * Spring Security 환경에서 JwtAuthenticationFilter를 통과한 것처럼 테스트하기 위해,
     * 실제 `JwtUtil` 내부에 정의된 `secretKey` 필드를 리플렉션을 통해 접근하여 서명 키를 추출하고
     * 해당 키로 서명된 JWT 토큰을 생성한다.
     * </p>
     *
     * @return 테스트용 유효한 JWT 문자열
     * @throws Exception 리플렉션 접근 또는 키 추출 과정에서 발생할 수 있는 예외
     */
    private String generateJwt() throws Exception {
        Field secretKeyField = jwtUtil.getClass().getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        SecretKey secretKey = (SecretKey) secretKeyField.get(jwtUtil);

        long now = System.currentTimeMillis();
        long expiration = 1000 * 60 * 10;

        return Jwts.builder()
                .subject(String.valueOf(TEST_USER_ID))
                .claim("username", TEST_USERNAME)
                .claim("userRole", TEST_ROLE)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .signWith(secretKey)
                .compact();
    }

    private CommentEntity createCommentEntity(String content) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setContentId(1L);
        commentEntity.setContent(content);
        commentEntity.setUserId(TEST_USER_ID);
        commentEntity.setUsername(TEST_USERNAME);
        commentEntity.setCreatedAt(Instant.now());
        commentEntity.setUpdatedAt(Instant.now());
        return commentEntity;
    }

}
