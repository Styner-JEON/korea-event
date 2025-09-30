package com.event.model.response;

import java.time.Instant;

public record CommentListResponse(
        Long commentId,
        Long contentId,
        Long userId,
        String username,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
}
