package com.event.controller;

import com.event.model.request.CommentInsertRequest;
import com.event.model.request.CommentUpdateRequest;
// removed unused import
import com.event.model.response.CommentResponse;
import com.event.model.response.CommentScrollResponse;
import com.event.security.CustomPrincipal;
import com.event.security.JwtAuthenticationFilter;
import com.event.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// removed unused import
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
// removed unused import
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CommentController 단위 테스트")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomPrincipal customPrincipal = new CustomPrincipal(1L, "tester");

    @BeforeEach
    void setUpSecurityContext() {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                customPrincipal,
                null,
                List.of());
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("getCommentsByContentId")
    class GetCommentListTest {
        @Test
        @DisplayName("댓글 Keyset 스크롤 정상 조회 - 2개 반환")
        void givenTwoComments_whenGetCommentScroll_thenReturnsTwo() throws Exception {
            // Given
            CommentResponse commentResponse = new CommentResponse(
                    1L, "내용1", 1L, 1L, "tester1", LocalDateTime.now(), LocalDateTime.now()
            );
            CommentResponse commentResponse2 = new CommentResponse(
                    2L, "내용2", 1L, 2L, "tester2", LocalDateTime.now(), LocalDateTime.now()
            );
            CommentScrollResponse scroll = new com.event.model.response.CommentScrollResponse(
                    List.of(commentResponse, commentResponse2), null
            );

            given(commentService.getCommentScrollByContentId(eq(1L), any())).willReturn(scroll);

            // When
            ResultActions resultActions = mockMvc.perform(get("/events/v1/1/comments").contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.commentResponseList.length()").value(2))
                    .andExpect(jsonPath("$.commentResponseList[0].commentId").value(1L))
                    .andExpect(jsonPath("$.commentResponseList[1].commentId").value(2L));

            then(commentService).should().getCommentScrollByContentId(eq(1L), any());
        }
    }

    @Nested
    @DisplayName("insertComment")
    class InsertCommentTest {
        @Test
        @DisplayName("정상적인 댓글 삽입")
        void givenValidRequest_whenInsertComment_thenReturns200() throws Exception {
            // Given
            CommentInsertRequest commentInsertRequest = new CommentInsertRequest("내용입니다");
            CommentResponse commentResponse = new CommentResponse(
                    1L,
                    "내용입니다",
                    1L,
                    1L,
                    "tester",
                    LocalDateTime.now(),
                    LocalDateTime.now());

            given(commentService.insertComment(eq(1L), any(), any())).willReturn(commentResponse);

            // When
            ResultActions result = mockMvc.perform(post("/events/v1/1/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commentInsertRequest)));

            // Then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.commentId").value(1L))
                    .andExpect(jsonPath("$.username").value("tester"));

            then(commentService).should().insertComment(eq(1L), any(), eq(customPrincipal));
        }

        @Test
        @DisplayName("댓글 내용이 1000자를 초과하면 400")
        void givenTooLongContent_whenInsertComment_thenReturns400() throws Exception {
            // Given
            String over1000 = "a".repeat(1001);
            CommentInsertRequest commentInsertRequest = new CommentInsertRequest(over1000);

            // When
            ResultActions resultActions = mockMvc.perform(post("/events/v1/1/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commentInsertRequest)));

            // Then
            resultActions.andDo(print()).andExpect(status().isBadRequest());

            then(commentService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("deleteComment")
    class DeleteCommentTest {
        @Test
        @DisplayName("정상적인 댓글 삭제")
        void givenAuthor_whenDeleteComment_thenReturns200() throws Exception {
            // Given
            CommentResponse commentResponse = new CommentResponse(
                    1L,
                    "삭제된 댓글",
                    1L,
                    1L,
                    "tester",
                    LocalDateTime.now(),
                    LocalDateTime.now());

            given(commentService.deleteComment(eq(1L), any())).willReturn(commentResponse);

            // When
            ResultActions resultActions = mockMvc.perform(delete("/events/v1/1/comments/1"));

            // Then
            resultActions.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.commentId").value(1L));

            then(commentService).should().deleteComment(eq(1L), eq(customPrincipal));
        }
    }

    @Nested
    @DisplayName("updateComment")
    class UpdateCommentTest {
        @Test
        @DisplayName("정상적인 댓글 수정")
        void givenValidRequest_whenUpdateComment_thenReturns200() throws Exception {
            // Given
            CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("수정 내용");
            CommentResponse commentResponse = new CommentResponse(
                    1L,
                    "수정 내용",
                    1L,
                    1L,
                    "tester",
                    LocalDateTime.now(),
                    LocalDateTime.now());

            given(commentService.updateComment(eq(1L), any(), any())).willReturn(commentResponse);

            // When
            ResultActions resultActions = mockMvc.perform(patch("/events/v1/1/comments/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commentUpdateRequest)));

            // Then
            resultActions.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.commentId").value(1L));

            then(commentService).should().updateComment(eq(1L), any(), eq(customPrincipal));
        }

        @Test
        @DisplayName("유효성 검증 실패 시 400")
        void givenInvalidRequest_whenUpdateComment_thenReturns400() throws Exception {
            // Given
            CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("");

            // When
            ResultActions resultActions = mockMvc.perform(patch("/events/v1/1/comments/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commentUpdateRequest)));

            // Then
            resultActions.andDo(print()).andExpect(status().isBadRequest());

            then(commentService).shouldHaveNoInteractions();
        }
    }

}
