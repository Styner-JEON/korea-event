package com.auth.model.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}
