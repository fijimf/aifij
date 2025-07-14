package com.fijimf.deepfij.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordValidatorTest {

    private PasswordValidator passwordValidator;

    @BeforeEach
    void setUp() {
        passwordValidator = new PasswordValidator();
    }

    @Test
    void validate_WithValidPassword_ShouldReturnValid() {
        String validPassword = "StrongPass123!";
        
        PasswordValidator.ValidationResult result = passwordValidator.validate(validPassword);
        
        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void validate_WithNullPassword_ShouldReturnInvalid() {
        PasswordValidator.ValidationResult result = passwordValidator.validate(null);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("Password is required");
    }

    @Test
    void validate_WithEmptyPassword_ShouldReturnInvalid() {
        PasswordValidator.ValidationResult result = passwordValidator.validate("");
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("Password is required");
    }

    @Test
    void validate_WithBlankPassword_ShouldReturnInvalid() {
        PasswordValidator.ValidationResult result = passwordValidator.validate("   ");
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("Password is required");
    }

    @Test
    void validate_WithTooShortPassword_ShouldReturnInvalid() {
        String shortPassword = "Short1!";
        
        PasswordValidator.ValidationResult result = passwordValidator.validate(shortPassword);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("Password must be at least 8 characters long");
    }

    @Test
    void validate_WithTooLongPassword_ShouldReturnInvalid() {
        String longPassword = "A".repeat(129) + "1!a";
        
        PasswordValidator.ValidationResult result = passwordValidator.validate(longPassword);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("Password must not exceed 128 characters");
    }

    @Test
    void validate_WithoutUppercase_ShouldReturnInvalid() {
        String password = "lowercase123!";
        
        PasswordValidator.ValidationResult result = passwordValidator.validate(password);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("Password must contain at least one uppercase letter");
    }

    @Test
    void validate_WithoutLowercase_ShouldReturnInvalid() {
        String password = "UPPERCASE123!";
        
        PasswordValidator.ValidationResult result = passwordValidator.validate(password);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("Password must contain at least one lowercase letter");
    }

    @Test
    void validate_WithoutDigit_ShouldReturnInvalid() {
        String password = "NoDigitHere!";
        
        PasswordValidator.ValidationResult result = passwordValidator.validate(password);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("Password must contain at least one digit");
    }

    @Test
    void validate_WithoutSpecialCharacter_ShouldReturnInvalid() {
        String password = "NoSpecialChar123";
        
        PasswordValidator.ValidationResult result = passwordValidator.validate(password);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("Password must contain at least one special character");
    }

    @Test
    void validate_WithCommonPassword_ShouldReturnInvalid() {
        String commonPassword = "password";
        
        PasswordValidator.ValidationResult result = passwordValidator.validate(commonPassword);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("Password is too common. Please choose a more secure password");
    }

    @Test
    void validate_WithCommonPasswordDifferentCase_ShouldReturnInvalid() {
        String commonPassword = "PASSWORD";
        
        PasswordValidator.ValidationResult result = passwordValidator.validate(commonPassword);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("Password is too common. Please choose a more secure password");
    }

    @Test
    void validate_WithMultipleViolations_ShouldReturnAllErrors() {
        String badPassword = "abc";
        
        PasswordValidator.ValidationResult result = passwordValidator.validate(badPassword);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).hasSize(4);
        assertThat(result.errors()).contains(
            "Password must be at least 8 characters long",
            "Password must contain at least one uppercase letter",
            "Password must contain at least one digit",
            "Password must contain at least one special character"
        );
    }

    @Test
    void validate_WithValidComplexPassword_ShouldReturnValid() {
        String complexPassword = "MyS3cur3P@ssw0rd!";
        
        PasswordValidator.ValidationResult result = passwordValidator.validate(complexPassword);
        
        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }
}