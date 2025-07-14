package com.fijimf.deepfij;

import com.fijimf.deepfij.validation.PasswordValidator;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class DeepFijApplicationTest {

    @Test
    void getTempAdminPassword_ShouldGenerateValidPassword() {
        // Clear any existing system property
        System.clearProperty("admin.password");
        
        // Use reflection to call the private method
        String password = (String) ReflectionTestUtils.invokeMethod(
            DeepFijApplication.class, 
            "getTempAdminPassword"
        );
        
        // Verify the password meets our complexity requirements
        PasswordValidator validator = new PasswordValidator();
        PasswordValidator.ValidationResult result = validator.validate(password);
        
        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
        assertThat(password).hasSize(8);
        
        // Verify it contains required character types
        assertThat(password).matches(".*[A-Z].*"); // uppercase
        assertThat(password).matches(".*[a-z].*"); // lowercase  
        assertThat(password).matches(".*[0-9].*"); // digit
        assertThat(password).matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"); // special char
    }
    
    @Test
    void getTempAdminPassword_WithSystemProperty_ShouldReturnSystemProperty() {
        String customPassword = "CustomPass123!";
        System.setProperty("admin.password", customPassword);
        
        try {
            String password = (String) ReflectionTestUtils.invokeMethod(
                DeepFijApplication.class, 
                "getTempAdminPassword"
            );
            
            assertThat(password).isEqualTo(customPassword);
        } finally {
            System.clearProperty("admin.password");
        }
    }
}