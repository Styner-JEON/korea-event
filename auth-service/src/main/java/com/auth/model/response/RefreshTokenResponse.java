package com.auth.model.response;

public record RefreshTokenResponse(String accessToken, long accessTokenExpiry, UserResponse user) {
}
