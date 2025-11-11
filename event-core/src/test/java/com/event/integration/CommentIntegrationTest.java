package com.event.integration;

import com.event.model.entity.CommentEntity;
import com.event.model.request.CommentInsertRequest;
import com.event.model.request.CommentUpdateRequest;
import com.event.repository.CommentRepository;
import com.event.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("댓글 통합 테스트")
class CommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Long TEST_USER_ID = 100L;

    private static final String TEST_USERNAME = "testuser";

    private static final String TEST_USER_ROLE = "ROLE_USER";

    private String jwt;

    @BeforeEach
    void setUp() throws Exception {
        jwt = generateJwt();
    }

    @AfterEach
    void tearDown() {
        commentRepository.deleteAll();
    }

    @Nested
    @DisplayName("댓글 조회")
    class GetCommentListTest {
        @Test
        @DisplayName("댓글이 여러 개 존재할 때, 댓글 목록이 정상 조회된다")
        void givenMultipleComments_whenGetCommentList_thenReturnsSlice() throws Exception {
            // Given
            commentRepository.save(createCommentEntity(TEST_USER_ID, TEST_USERNAME, "댓글 1"));
            commentRepository.save(createCommentEntity(TEST_USER_ID, TEST_USERNAME, "댓글 2"));

            // When
            ResultActions resultActions = mockMvc.perform(get("/events/v1/1/comments")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.commentResponseList.length()").value(2))
                    .andExpect(jsonPath("$.commentResponseList[*].content",
                            containsInAnyOrder("댓글 1", "댓글 2")));
        }

        @Test
        @DisplayName("댓글이 존재하지 않을 때, 빈 리스트가 반환된다")
        void givenNoComments_whenGetCommentList_thenReturnsEmptySlice() throws Exception {
            // When
            ResultActions resultActions = mockMvc.perform(get("/events/v1/1/comments")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.commentResponseList.length()").value(0))
                    .andExpect(jsonPath("$.nextCursor").value((Object) null));
        }
    }

    @Nested
    @DisplayName("댓글 등록")
    class InsertCommentTest {
        @Test
        @DisplayName("유효한 토큰이 주어졌을 때, 댓글 등록에 성공한다")
        void givenValidToken_whenPostComment_thenReturns200() throws Exception {
            // Given
            CommentInsertRequest commentInsertRequest = new CommentInsertRequest("통합테스트 댓글");

            // When
            ResultActions resultActions = mockMvc.perform(post("/events/v1/1/comments")
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commentInsertRequest)));

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(TEST_USERNAME));

            assertThat(commentRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("토큰이 없을 때, 댓글 등록은 401 Unauthorized를 반환한다")
        void givenNoToken_whenPostComment_thenReturns401() throws Exception {
            // Given
            CommentInsertRequest commentInsertRequest = new CommentInsertRequest("무단 등록 시도");

            // When
            ResultActions resultActions = mockMvc.perform(post("/events/v1/1/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commentInsertRequest)));

            // Then
            resultActions.andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteCommentTest {

        @Test
        @DisplayName("작성자가 삭제 요청하면 200 OK")
        void givenAuthorUser_whenDeleteComment_thenReturns200() throws Exception {
            // Given
            CommentEntity commentEntity = commentRepository.save(createCommentEntity(TEST_USER_ID, TEST_USERNAME, "삭제 댓글"));

            // When
            ResultActions resultActions = mockMvc.perform(delete("/events/v1/1/comments/" + commentEntity.getCommentId())
                    .header("Authorization", "Bearer " + jwt));

            // Then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$.commentId").value(commentEntity.getCommentId()));

            assertThat(commentRepository.existsById(commentEntity.getCommentId())).isFalse();
        }

        @Test
        @DisplayName("토큰 없이 삭제 시도 시 401 Unauthorized를 반환한다")
        void givenNoToken_whenDeleteComment_thenReturns401() throws Exception {
            // Given

            // When
            ResultActions resultActions = mockMvc.perform(delete("/events/v1/1/comments/1"));

            // Then
            resultActions.andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateCommentTest {
        @Test
        @DisplayName("작성자가 수정 요청 시 200 OK를 반환하고, 내용이 실제로 수정된다")
        void givenAuthorUser_whenPutComment_thenReturns200AndContentIsUpdated() throws Exception {
            // Given
            CommentEntity commentEntity = commentRepository.save(createCommentEntity(TEST_USER_ID, TEST_USERNAME, "수정 전"));
            CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("수정 완료!");

            // When
            ResultActions resultActions = mockMvc.perform(patch("/events/v1/1/comments/" + commentEntity.getCommentId())
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commentUpdateRequest)));

            // Then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$.commentId").value(commentEntity.getCommentId()))
                    .andExpect(jsonPath("$.username").value(TEST_USERNAME));

            CommentEntity updatedComment = commentRepository.findById(commentEntity.getCommentId())
                    .orElseThrow();
            assertThat(updatedComment.getContent()).isEqualTo("수정 완료!");
        }

        @Test
        @DisplayName("작성자가 아닌 사용자가 수정 시도 시 403을 반환한다")
        void givenNonAuthorUser_whenPutComment_thenReturns403() throws Exception {
            // Given
            CommentEntity commentEntity = commentRepository.save(createCommentEntity(888L, "otherUser", "타인 댓글"));
            CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("수정 시도");

            // When
            ResultActions resultActions = mockMvc.perform(patch("/events/v1/1/comments/" + commentEntity.getCommentId())
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commentUpdateRequest)));

            // Then
            resultActions.andExpect(status().isForbidden());
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
        long expirationTime = 1000 * 60 * 10; // 1000 * 60 * 10 = 10분

        return Jwts.builder()
                .subject(String.valueOf(TEST_USER_ID))
                .claim("username", TEST_USERNAME)
                .claim("userRole", TEST_USER_ROLE)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    private CommentEntity createCommentEntity(Long userId, String username, String content) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setContentId(1L);
        commentEntity.setContent(content);
        commentEntity.setUserId(userId);
        commentEntity.setUsername(username);
        commentEntity.setCreatedAt(Instant.now());
        commentEntity.setUpdatedAt(Instant.now());
        return commentEntity;
    }

}
