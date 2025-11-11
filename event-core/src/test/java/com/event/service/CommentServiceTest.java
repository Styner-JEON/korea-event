package com.event.service;

import com.event.exception.CustomCommentException;
import com.event.mapper.CommentMapper;
import com.event.model.entity.CommentEntity;
import com.event.model.request.CommentInsertRequest;
import com.event.model.request.CommentUpdateRequest;
import com.event.model.response.CommentResponse;
import com.event.model.response.CommentScrollResponse;
import com.event.repository.CommentRepository;
import com.event.security.CustomPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 단위 테스트")
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    private final CustomPrincipal customPrincipal = new CustomPrincipal(1L, "tester");

    @Nested
    @DisplayName("getCommentListByContentId")
    class GetCommentListTest {

        @BeforeEach
        void init() {
            ReflectionTestUtils.setField(commentService, "commentSize", 20);
            ReflectionTestUtils.setField(commentService, "commentSortDirection", "DESC");
            ReflectionTestUtils.setField(commentService, "commentSortProperty", "commentId");
        }

        @Test
        @DisplayName("Keyset 스크롤 조회 시 댓글 2개 반환(마지막 페이지)")
        void givenTwoComments_whenGetCommentScroll_thenReturnsTwo() {
            // Given
            CommentEntity commentEntity = createCommentEntity(1L, 10L, 1L, "tester1");
            CommentEntity commentEntity2 = createCommentEntity(2L, 10L, 2L, "tester2");
            List<CommentEntity> commentEntityList = List.of(commentEntity, commentEntity2);

            Window<CommentEntity> commentEntityWindow = Window.<CommentEntity>from(commentEntityList, i -> ScrollPosition.keyset(), false);

            CommentResponse commentResponse = new CommentResponse(1L, "내용1", 10L, 1L, "tester1", LocalDateTime.now(), LocalDateTime.now());
            CommentResponse commentResponse2 = new CommentResponse(2L, "내용2", 10L, 2L, "tester2", LocalDateTime.now(), LocalDateTime.now());

            given(commentRepository.findScrollByContentId(eq(10L), any(), any(), any()))
                    .willReturn(commentEntityWindow);
            given(commentMapper.toCommentResponse(commentEntity)).willReturn(commentResponse);
            given(commentMapper.toCommentResponse(commentEntity2)).willReturn(commentResponse2);

            // When
            CommentScrollResponse result = commentService.getCommentScrollByContentId(10L, null);

            // Then
            assertThat(result.commentResponseList()).hasSize(2);
            assertThat(result.commentResponseList().get(0).commentId()).isEqualTo(1L);
            assertThat(result.commentResponseList().get(1).commentId()).isEqualTo(2L);
            assertThat(result.nextCursor()).isNull();

            then(commentRepository).should().findScrollByContentId(eq(10L), any(), any(), any());
            then(commentMapper).should().toCommentResponse(commentEntity);
            then(commentMapper).should().toCommentResponse(commentEntity2);
        }
    }

    @Nested
    @DisplayName("insertComment")
    class InsertCommentTest {
        @Test
        @DisplayName("댓글 정상 등록")
        void givenValidRequest_whenInsertComment_thenReturnsResponse() {
            // Given
            CommentInsertRequest commentInsertRequest = new CommentInsertRequest("좋은 글입니다.");
            CommentEntity commentEntity = createCommentEntity(100L, 1L, 1L, "tester");
            CommentResponse commentResponse = new CommentResponse(
                    100L,
                    "좋은 글입니다.",
                    1L,
                    1L,
                    "tester",
                    LocalDateTime.now(),
                    LocalDateTime.now());

            given(commentRepository.save(any())).willReturn(commentEntity);
            given(commentMapper.toCommentResponse(commentEntity)).willReturn(commentResponse);

            // When
            CommentResponse result = commentService.insertComment(1L, commentInsertRequest, customPrincipal);

            // Then
            assertThat(result.commentId()).isEqualTo(100L);
            assertThat(result.username()).isEqualTo("tester");

            then(commentRepository).should().save(any());
            then(commentMapper).should().toCommentResponse(commentEntity);
        }
    }

    @Nested
    @DisplayName("deleteComment")
    class DeleteCommentTest {
        @Test
        @DisplayName("댓글 정상 삭제")
        void givenAuthorUser_whenDeleteComment_thenSuccess() {
            // Given
            CommentEntity commentEntity = createCommentEntity(1L, 10L, 1L, "tester");

            given(commentRepository.findById(1L)).willReturn(Optional.of(commentEntity));
            willDoNothing().given(commentRepository).delete(commentEntity);
            given(commentMapper.toCommentResponse(commentEntity))
                    .willReturn(new CommentResponse(
                            1L,
                            "샘플 댓글",
                            10L,
                            1L,
                            "tester",
                            LocalDateTime.now(),
                            LocalDateTime.now()));

            // When
            CommentResponse commentResponse = commentService.deleteComment(1L, customPrincipal);

            // Then
            assertThat(commentResponse.commentId()).isEqualTo(1L);

            then(commentRepository).should().findById(1L);
            then(commentRepository).should().delete(commentEntity);
            then(commentMapper).should().toCommentResponse(commentEntity);
        }

        @Test
        @DisplayName("댓글 작성자가 아닌 자가 댓글 삭제 시도")
        void givenOtherUser_whenDeleteComment_thenThrowsException() {
            // Given
            CommentEntity commentEntity = createCommentEntity(1L, 10L, 999L, "notOwner");

            given(commentRepository.findById(1L)).willReturn(Optional.of(commentEntity));

            // When & Then
            assertThatThrownBy(() -> commentService.deleteComment(1L, customPrincipal))
                    .isInstanceOf(CustomCommentException.class);

            then(commentRepository).should().findById(1L);
            then(commentRepository).should(never()).delete(any());
        }

        @Test
        @DisplayName("없는 댓글을 삭제 시도")
        void givenInvalidId_whenDeleteComment_thenThrowsException() {
            // Given
            Long invalidCommentId = 999L;
            given(commentRepository.findById(invalidCommentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.deleteComment(invalidCommentId, customPrincipal))
                    .isInstanceOf(CustomCommentException.class);

            then(commentRepository).should().findById(invalidCommentId);
            then(commentRepository).should(never()).delete(any());
        }
    }

    @Nested
    @DisplayName("updateComment")
    class UpdateCommentTest {
        @Test
        @DisplayName("댓글 정상 수정")
        void givenAuthorUser_whenUpdateComment_thenSuccess() {
            // Given
            CommentEntity commentEntity = createCommentEntity(1L, 10L, 1L, "tester");
            CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("수정된 댓글입니다.");

            given(commentRepository.findById(1L)).willReturn(Optional.of(commentEntity));
            given(commentRepository.save(any())).willReturn(commentEntity);
            given(commentMapper.toCommentResponse(commentEntity))
                    .willReturn(new CommentResponse(
                            1L,
                            "수정된 댓글입니다.",
                            10L,
                            1L,
                            "tester",
                            LocalDateTime.now(),
                            LocalDateTime.now()));

            // When
            CommentResponse commentResponse = commentService.updateComment(1L, commentUpdateRequest, customPrincipal);

            // Then
            assertThat(commentResponse.contentId()).isEqualTo(10L);
            assertThat(commentEntity.getContent()).isEqualTo("수정된 댓글입니다.");
            assertThat(commentEntity.getUpdatedAt()).isNotNull();

            then(commentRepository).should().findById(1L);
            then(commentRepository).should().save(commentEntity);
            then(commentMapper).should().toCommentResponse(commentEntity);
        }

        @Test
        @DisplayName("댓글 작성자가 아닌 자가 댓글 수정 시도")
        void givenOtherUser_whenUpdateComment_thenThrowsException() {
            // Given
            CommentEntity commentEntity = createCommentEntity(1L, 10L, 999L, "notOwner");
            CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("해킹 시도");

            given(commentRepository.findById(1L)).willReturn(Optional.of(commentEntity));

            // When & Then
            assertThatThrownBy(() -> commentService.updateComment(1L, commentUpdateRequest, customPrincipal))
                    .isInstanceOf(CustomCommentException.class);

            then(commentRepository).should().findById(1L);
            then(commentRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("없는 댓글을 수정 시도")
        void givenInvalidId_whenUpdateComment_thenThrowsException() {
            // Given
            Long invalidCommentId = 999L;
            CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("없는 댓글 수정");

            given(commentRepository.findById(invalidCommentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(
                    () -> commentService.updateComment(invalidCommentId, commentUpdateRequest, customPrincipal))
                    .isInstanceOf(CustomCommentException.class);

            then(commentRepository).should().findById(invalidCommentId);
            then(commentRepository).should(never()).save(any());
        }
    }

    private CommentEntity createCommentEntity(Long commentId, Long contentId, Long userId, String username) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setCommentId(commentId);
        commentEntity.setContentId(contentId);
        commentEntity.setUserId(userId);
        commentEntity.setUsername(username);
        commentEntity.setContent("샘플 댓글");
        commentEntity.setCreatedAt(Instant.now());
        commentEntity.setUpdatedAt(Instant.now());
        return commentEntity;
    }

}
