package com.event.model.response;

public record CommentResponse(
        Long commentId,
        Long contentId,
        Long userId,
        String username
) {
}
