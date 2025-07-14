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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentMapper commentMapper;

    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse insertComment(CommentInsertRequest commentInsertRequest, CustomPrincipal customPrincipal) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setCommentId(null);
        commentEntity.setContent(commentInsertRequest.content());
        commentEntity.setContentId(commentInsertRequest.contentId());
        commentEntity.setUserId(customPrincipal.userId());
        commentEntity.setUsername(customPrincipal.username());
        LocalDateTime now = LocalDateTime.now();
        commentEntity.setCreatedAt(now);
        commentEntity.setUpdatedAt(now);

        CommentEntity savedCommentEntity = commentRepository.save(commentEntity);
        log.info("DB insert completed for contentId: {}", commentEntity.getContentId());

        return commentMapper.toCommentResponse(savedCommentEntity);
    }

    @Transactional(readOnly = true)
    public Slice<CommentListResponse> getCommentsByContentId(Long contentId, Pageable pageable) {
        return commentRepository.findByContentId(contentId, pageable).map(commentMapper::toCommentListDto);
    }

    @Transactional
    public CommentResponse deleteComment(Long commentId, CustomPrincipal customPrincipal) {
        CommentEntity commentEntity = commentRepository.findById(commentId).orElseThrow(() -> {
            log.error("Comment not found with commentId: {}", commentId);
            return new CustomCommentException(HttpStatus.NOT_FOUND, "Comment not found with commentId: " + commentId);
        });

        if (!commentEntity.getUserId().equals(customPrincipal.userId())) {
            throw new CustomCommentException(HttpStatus.FORBIDDEN, "No permission to delete this comment.");
        }

        commentRepository.delete(commentEntity);
        return commentMapper.toCommentResponse(commentEntity);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest commentUpdateRequest,
            CustomPrincipal customPrincipal) {
        CommentEntity commentEntity = commentRepository.findById(commentId).orElseThrow(() -> {
            log.error("Comment not found with commentId: {}", commentId);
            return new CustomCommentException(HttpStatus.NOT_FOUND, "Comment not found with commentId: " + commentId);
        });

        if (!commentEntity.getUserId().equals(customPrincipal.userId())) {
            throw new CustomCommentException(HttpStatus.FORBIDDEN, "No permission to update this comment.");
        }

        commentEntity.setContent(commentUpdateRequest.content());
        commentEntity.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(commentEntity);

        return commentMapper.toCommentResponse(commentEntity);
    }

}
