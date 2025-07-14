package com.fijimf.deepfij.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
    @NotBlank(message = "Username is required")
    String username
) {
    public ForgotPasswordRequest {
        if (username != null && username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
    }
}