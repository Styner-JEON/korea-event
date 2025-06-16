package com.auth.model.response;

public record UserResponse(
        Long id,
        String name,
        String role
) {
}
