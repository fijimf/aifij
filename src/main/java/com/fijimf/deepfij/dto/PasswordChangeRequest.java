package com.fijimf.deepfij.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
    @NotBlank(message = "Current password is required")
    String currentPassword,
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 128, message = "New password must be between 8 and 128 characters")
    String newPassword
) {
    public PasswordChangeRequest {
        if (currentPassword != null && currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Current password cannot be blank");
        }
        if (newPassword != null && newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be blank");
        }
    }
}