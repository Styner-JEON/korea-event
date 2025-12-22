package com.auth.model.response;

public record SignupOtpResponse(
        String signupAttemptId,
        long expiresInSeconds,
        long cooldownSeconds
) {
}
