package com.event.controller;

import com.event.model.request.CommentInsertRequest;
import com.event.model.request.CommentUpdateRequest;
import com.event.model.response.CommentListResponse;
import com.event.model.response.CommentResponse;
import com.event.security.CustomPrincipal;
import com.event.security.JwtAuthenticationFilter;
import com.event.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
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
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(
                        customPrincipal,
                        null,
                        List.of()
                );
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
        @DisplayName("댓글 목록 정상 조회 - 2개 반환")
        void givenTwoComments_whenGetCommentList_thenReturnsTwo() throws Exception {
            // Given
            CommentListResponse commentListResponse = new CommentListResponse(1L, 1L, 1L, "tester1", "내용1", LocalDateTime.now(), LocalDateTime.now());
            CommentListResponse commentListResponse2 = new CommentListResponse(2L, 1L, 2L, "tester2", "내용2", LocalDateTime.now(), LocalDateTime.now());
            PageRequest pageRequest = PageRequest.of(0, 20);
            Slice<CommentListResponse> commentListResponseSlice = new SliceImpl<>(List.of(commentListResponse, commentListResponse2), pageRequest, false);

            given(commentService.getCommentsByContentId(eq(1L), any())).willReturn(commentListResponseSlice);

            // When
            ResultActions resultActions = mockMvc.perform(get("/events/v1/1/comments").contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].commentId").value(1L))
                    .andExpect(jsonPath("$.content[1].commentId").value(2L));

            then(commentService).should().getCommentsByContentId(eq(1L), any());
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
            CommentResponse commentResponse = new CommentResponse(1L, 1L, 1L, "tester");

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
            CommentResponse commentResponse = new CommentResponse(1L, 1L, 1L, "tester");

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
            CommentResponse commentResponse = new CommentResponse(1L, 1L, 1L, "tester");

            given(commentService.updateComment(eq(1L), any(), any())).willReturn(commentResponse);

            // When
            ResultActions resultActions = mockMvc.perform(put("/events/v1/1/comments/1")
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
            ResultActions resultActions = mockMvc.perform(put("/events/v1/1/comments/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commentUpdateRequest)));

            // Then
            resultActions.andDo(print()).andExpect(status().isBadRequest());

            then(commentService).shouldHaveNoInteractions();
        }

    }

}
