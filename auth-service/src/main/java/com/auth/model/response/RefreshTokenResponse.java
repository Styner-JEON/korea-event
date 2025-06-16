package com.auth.model.response;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}
