package com.fijimf.deepfij.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
    @NotBlank(message = "Token is required")
    String token
) {
    public TokenRefreshRequest {
        if (token != null && token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be blank");
        }
    }
}