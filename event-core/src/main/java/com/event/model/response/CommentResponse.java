package com.event.model.response;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        String content,
        Long contentId,
        Long userId,
        String username,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}