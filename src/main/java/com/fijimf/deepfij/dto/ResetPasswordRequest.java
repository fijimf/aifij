package com.fijimf.deepfij.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "Reset token is required")
    String resetToken,
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 128, message = "New password must be between 8 and 128 characters")
    String newPassword
) {
    public ResetPasswordRequest {
        if (resetToken != null && resetToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Reset token cannot be blank");
        }
        if (newPassword != null && newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be blank");
        }
    }
}