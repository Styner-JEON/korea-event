package com.event.model.response;

public record EventFavoriteResponse(
        boolean favoriteStatus,
        Long contentId,
        Long userId
) {
}
