package com.event.model.response;

import java.time.Instant;

public record CommentResponse(
        Long commentId,
        String content,
        Long contentId,
        Long userId,
        String username,
        Instant createdAt,
        Instant updatedAt
) {
}