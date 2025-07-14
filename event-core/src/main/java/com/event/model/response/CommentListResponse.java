package com.event.model.response;

import java.time.LocalDateTime;

public record CommentListResponse(
        Long commentId,
        Long contentId,
        Long userId,
        String username,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
