package com.event.service;

import com.event.exception.CustomCommentException;
import com.event.mapper.CommentMapper;
import com.event.model.entity.CommentEntity;
import com.event.model.request.CommentInsertRequest;
import com.event.model.request.CommentUpdateRequest;
import com.event.model.response.CommentListResponse;
import com.event.model.response.CommentResponse;
import com.event.repository.CommentRepository;
import com.event.security.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 댓글 관련 비즈니스 로직을 처리하는 서비스
 * 
 * 댓글의 CRUD 기능을 제공하며, 사용자 권한 검증과
 * 댓글 분석 캐쉬 무효화 처리를 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentMapper commentMapper;

    private final CommentRepository commentRepository;

    /**
     * 특정 이벤트의 댓글 목록을 조회합니다.
     * 
     * @param contentId 이벤트 컨텐츠 ID
     * @param pageable  페이지네이션 정보
     * @return 댓글 목록 슬라이스
     */
    @Transactional(readOnly = true)
    public Slice<CommentListResponse> getCommentsByContentId(Long contentId, Pageable pageable) {
        return commentRepository.findByContentId(contentId, pageable).map(commentMapper::toCommentListResponse);
    }

    /**
     * 새 댓글을 등록합니다.
     * 댓글 등록 후 해당 이벤트의 댓글 분석 캐쉬를 무효화합니다.
     * 
     * @param contentId            이벤트 컨텐츠 ID
     * @param commentInsertRequest 댓글 등록 요청 정보
     * @param customPrincipal      인증된 사용자 정보
     * @return 등록된 댓글 정보
     */
    @Transactional
    @CacheEvict(value = "comment-analysis", key = "#contentId")
    public CommentResponse insertComment(Long contentId, CommentInsertRequest commentInsertRequest,
            CustomPrincipal customPrincipal) {
        CommentEntity commentEntity = createCommentEntity(contentId, commentInsertRequest, customPrincipal);
        CommentEntity savedCommentEntity = commentRepository.save(commentEntity);
        log.info("DB insert completed for contentId: {}", commentEntity.getContentId());

        return commentMapper.toCommentResponse(savedCommentEntity);
    }

    /**
     * 댓글을 삭제합니다.
     * 작성자 본인만 삭제할 수 있으며, 삭제 후 댓글 분석 캐쉬를 무효화합니다.
     * 
     * @param commentId       삭제할 댓글 ID
     * @param customPrincipal 인증된 사용자 정보
     * @return 삭제된 댓글 정보
     * @throws CustomCommentException 댓글이 존재하지 않거나 삭제 권한이 없을 경우
     */
    @Transactional
    @CacheEvict(value = "comment-analysis", key = "#result.contentId")
    public CommentResponse deleteComment(Long commentId, CustomPrincipal customPrincipal) {
        CommentEntity commentEntity = commentRepository.findById(commentId).orElseThrow(() -> {
            log.error("Delete attempt failed. Comment not found. commentId={}", commentId);
            return new CustomCommentException(
                    HttpStatus.NOT_FOUND,
                    "Delete attempt failed. Comment not found with commentId: " + commentId);
        });

        if (!commentEntity.getUserId().equals(customPrincipal.userId())) {
            throw new CustomCommentException(HttpStatus.FORBIDDEN, "No permission to delete this comment.");
        }

        commentRepository.delete(commentEntity);
        return commentMapper.toCommentResponse(commentEntity);
    }

    /**
     * 댓글을 수정합니다.
     * 작성자 본인만 수정할 수 있으며, 수정 후 댓글 분석 캐쉬를 무효화합니다.
     * 
     * @param commentId            수정할 댓글 ID
     * @param commentUpdateRequest 댓글 수정 요청 정보
     * @param customPrincipal      인증된 사용자 정보
     * @return 수정된 댓글 정보
     * @throws CustomCommentException 댓글이 존재하지 않거나 수정 권한이 없을 경우
     */
    @Transactional
    @CacheEvict(value = "comment-analysis", key = "#result.contentId")
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest commentUpdateRequest,
            CustomPrincipal customPrincipal) {
        CommentEntity commentEntity = commentRepository.findById(commentId).orElseThrow(() -> {
            log.error("Update attempt failed. Comment not found. commentId={}", commentId);
            return new CustomCommentException(
                    HttpStatus.NOT_FOUND,
                    "Update attempt failed. Comment not found with commentId: " + commentId);
        });

        if (!commentEntity.getUserId().equals(customPrincipal.userId())) {
            throw new CustomCommentException(HttpStatus.FORBIDDEN, "No permission to update this comment.");
        }

        commentEntity.setContent(commentUpdateRequest.content());
        commentEntity.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(commentEntity);

        return commentMapper.toCommentResponse(commentEntity);
    }

    private CommentEntity createCommentEntity(
            Long contentId,
            CommentInsertRequest commentInsertRequest,
            CustomPrincipal customPrincipal
    ) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setCommentId(null);
        commentEntity.setContent(commentInsertRequest.content());
        commentEntity.setContentId(contentId);
        commentEntity.setUserId(customPrincipal.userId());
        commentEntity.setUsername(customPrincipal.username());
        LocalDateTime now = LocalDateTime.now();
        commentEntity.setCreatedAt(now);
        commentEntity.setUpdatedAt(now);
        return commentEntity;
    }

}
