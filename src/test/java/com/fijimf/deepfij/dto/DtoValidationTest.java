package com.fijimf.deepfij.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void passwordChangeRequest_WithValidData_ShouldPassValidation() {
        PasswordChangeRequest request = new PasswordChangeRequest("currentPass123", "newPass123!");
        
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);
        
        assertThat(violations).isEmpty();
    }

    @Test
    void passwordChangeRequest_WithNullCurrentPassword_ShouldFailValidation() {
        PasswordChangeRequest request = new PasswordChangeRequest(null, "newPass123!");
        
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Current password is required");
    }

    @Test
    void passwordChangeRequest_WithNullNewPassword_ShouldFailValidation() {
        PasswordChangeRequest request = new PasswordChangeRequest("currentPass123", null);
        
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);
        
        assertThat(violations).hasSize(1); // Only NotBlank violation (Size doesn't apply to null)
        assertThat(violations.iterator().next().getMessage()).contains("New password is required");
    }

    @Test
    void passwordChangeRequest_WithTooShortNewPassword_ShouldFailValidation() {
        PasswordChangeRequest request = new PasswordChangeRequest("currentPass123", "short");
        
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must be between 8 and 128 characters");
    }

    @Test
    void passwordChangeRequest_WithTrimmedBlankPasswords_ShouldThrowException() {
        assertThatThrownBy(() -> new PasswordChangeRequest("   ", "newPass123!"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Current password cannot be blank");
            
        assertThatThrownBy(() -> new PasswordChangeRequest("currentPass123", "   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("New password cannot be blank");
    }

    @Test
    void tokenRefreshRequest_WithValidToken_ShouldPassValidation() {
        TokenRefreshRequest request = new TokenRefreshRequest("valid.jwt.token");
        
        Set<ConstraintViolation<TokenRefreshRequest>> violations = validator.validate(request);
        
        assertThat(violations).isEmpty();
    }

    @Test
    void tokenRefreshRequest_WithNullToken_ShouldFailValidation() {
        TokenRefreshRequest request = new TokenRefreshRequest(null);
        
        Set<ConstraintViolation<TokenRefreshRequest>> violations = validator.validate(request);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Token is required");
    }

    @Test
    void tokenRefreshRequest_WithTrimmedBlankToken_ShouldThrowException() {
        assertThatThrownBy(() -> new TokenRefreshRequest("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Token cannot be blank");
    }

    @Test
    void userProfileResponse_WithValidData_ShouldCreateSuccessfully() {
        UserProfileResponse response = new UserProfileResponse(1L, "testuser", true, Set.of("USER", "ADMIN"));
        
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.enabled()).isTrue();
        assertThat(response.roles()).containsExactlyInAnyOrder("USER", "ADMIN");
    }
}