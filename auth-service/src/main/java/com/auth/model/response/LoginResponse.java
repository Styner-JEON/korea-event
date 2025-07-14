package com.auth.model.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiry,
        long refreshTokenExpiry,
        UserResponse user
) {
}
