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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Value("${size.comment}")
    private int commentSize;

    @Value("${sort.comment.direction}")
    private String commentSortDirection;

    @Value("${sort.comment.property}")
    private String commentSortProperty;

    /**
     * 특정 이벤트의 댓글 수를 조회합니다.
     * 
     * @param contentId 이벤트 컨텐츠 ID
     * @return 댓글 수
     */
    @Transactional(readOnly = true)
    public int getCommentCount(Long contentId) {
        return commentRepository.countByContentId(contentId);
    }

    /**
     * 특정 이벤트의 댓글 목록을 조회합니다.
     * 
     * @param contentId 이벤트 컨텐츠 ID
     * @param pageable  페이지네이션 정보
     * @return 댓글 목록 페이지
     */
    // @Transactional(readOnly = true)
    // public Page<CommentListResponse> getCommentPageByContentId(Long contentId,
    // Pageable pageable) {
    // return commentRepository.findPageByContentId(contentId,
    // pageable).map(commentMapper::toCommentListResponse);
    // }

    /**
     * 특정 이벤트의 댓글 목록을 Slice로 조회합니다.
     *
     * @param contentId 이벤트 컨텐츠 ID
     * @param pageable  페이지네이션 정보
     * @return 댓글 목록 슬라이스
     */
    // @Transactional(readOnly = true)
    // public Slice<CommentListResponse> getCommentSliceByContentId(Long contentId,
    // Pageable pageable) {
    // return commentRepository.findSliceByContentId(contentId,
    // pageable).map(commentMapper::toCommentListResponse);
    // }

    /**
     * 특정 이벤트의 댓글 목록을 Keyset-Filtering 방식으로 조회합니다.
     * 
     * @param contentId
     * @param cursor
     * @return 댓글 목록과 다음 커서
     */
    @Transactional(readOnly = true)
    public CommentScrollResponse getCommentScrollByContentId(Long contentId, String cursor) {
        KeysetScrollPosition keysetScrollPosition = (cursor == null || cursor.isBlank())
                ? ScrollPosition.keyset()
                : decodeCursor(cursor);

        Sort.Direction direction = Sort.Direction.fromString(commentSortDirection);
        Sort sort = Sort.by(direction, commentSortProperty).and(Sort.by(Sort.Direction.DESC, "commentId"));

        Limit limit = Limit.of(commentSize);

        Window<CommentEntity> commentEntityWindow = commentRepository.findScrollByContentId(
                contentId,
                sort,
                limit,
                keysetScrollPosition
        );

        List<CommentResponse> commentResponseList = commentEntityWindow.getContent().stream()
                .map(commentMapper::toCommentResponse).toList();

        String nextCursor = null;
        // 조회한 페이지가 비어 있지 않고, 마지막 페이지가 아닌 경우
        if (!commentEntityWindow.isEmpty() && !commentEntityWindow.isLast()) {
            CommentEntity lastCommentEntity = commentEntityWindow.getContent().get(commentEntityWindow.size() - 1);
            nextCursor = encodeCursor(
                    lastCommentEntity.getUpdatedAt(),
                    lastCommentEntity.getCommentId()
            );
        }

        return new CommentScrollResponse(commentResponseList, nextCursor);
    }

    /**
     * 커서를 인코딩합니다.
     *
     * @param updatedAt
     * @param commentId
     * @return Base64URL 인코딩된 커서 문자열
     */
    private String encodeCursor(Instant updatedAt, Long commentId) {
        long epochMillis = updatedAt.toEpochMilli();
        String rawCursor = epochMillis + "|" + commentId;

        return Base64.getUrlEncoder().encodeToString(rawCursor.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 커서를 디코딩합니다.
     *
     * @param cursor
     * @return KeysetScrollPosition 객체
     */
    private KeysetScrollPosition decodeCursor(String cursor) {
        try {
            String rawCursor = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = rawCursor.split("\\|", 2);
            long epochMillis = Long.parseLong(parts[0]);
            long commentId = Long.parseLong(parts[1]);

            Instant updatedAt = Instant.ofEpochMilli(epochMillis);

            Map<String, Object> key = new LinkedHashMap<>();
            key.put(commentSortProperty, updatedAt);
            key.put("commentId", commentId);

            return ScrollPosition.forward(key);
        } catch (NumberFormatException e) {
            log.error("Invalid cursor format: {}", cursor, e);
            throw new CustomCommentException(HttpStatus.BAD_REQUEST, "Invalid cursor format");
        }
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

        long userId = customPrincipal.userId();
        if (!commentEntity.getUserId().equals(userId)) {
            log.error("No permission to delete this comment. userId={} commentId={}", userId, commentId);
            throw new CustomCommentException(
                    HttpStatus.FORBIDDEN,
                    "No permission to delete this comment. userId=" + userId + " commentId=" + commentId);
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

        long userId = customPrincipal.userId();
        if (!commentEntity.getUserId().equals(userId)) {
            log.error("No permission to update this comment. userId={} commentId={}", userId, commentId);
            throw new CustomCommentException(
                    HttpStatus.FORBIDDEN,
                    "No permission to update this comment. userId=" + userId + " commentId=" + commentId);
        }

        commentEntity.setContent(commentUpdateRequest.content());
        commentEntity.setUpdatedAt(Instant.now());
        commentRepository.save(commentEntity);

        return commentMapper.toCommentResponse(commentEntity);
    }

    private CommentEntity createCommentEntity(
            Long contentId,
            CommentInsertRequest commentInsertRequest,
            CustomPrincipal customPrincipal) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setCommentId(null);
        commentEntity.setContent(commentInsertRequest.content());
        commentEntity.setContentId(contentId);
        commentEntity.setUserId(customPrincipal.userId());
        commentEntity.setUsername(customPrincipal.username());
        Instant now = Instant.now();
        commentEntity.setCreatedAt(now);
        commentEntity.setUpdatedAt(now);
        return commentEntity;
    }

}
