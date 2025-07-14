package com.fijimf.deepfij.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetServiceTest {

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService();
    }

    @Test
    void generateResetToken_ShouldReturnValidToken() {
        String username = "testuser";
        
        String token = passwordResetService.generateResetToken(username);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(passwordResetService.getActiveTokenCount()).isEqualTo(1);
    }

    @Test
    void validateResetToken_WithValidToken_ShouldReturnUsername() {
        String username = "testuser";
        String token = passwordResetService.generateResetToken(username);
        
        String validatedUsername = passwordResetService.validateResetToken(token);
        
        assertThat(validatedUsername).isEqualTo(username);
    }

    @Test
    void validateResetToken_WithInvalidToken_ShouldReturnNull() {
        String invalidToken = "invalid-token";
        
        String username = passwordResetService.validateResetToken(invalidToken);
        
        assertThat(username).isNull();
    }

    @Test
    void consumeResetToken_ShouldRemoveToken() {
        String username = "testuser";
        String token = passwordResetService.generateResetToken(username);
        
        passwordResetService.consumeResetToken(token);
        
        assertThat(passwordResetService.validateResetToken(token)).isNull();
        assertThat(passwordResetService.getActiveTokenCount()).isEqualTo(0);
    }

    @Test
    void clearAll_ShouldRemoveAllTokens() {
        passwordResetService.generateResetToken("user1");
        passwordResetService.generateResetToken("user2");
        
        passwordResetService.clearAll();
        
        assertThat(passwordResetService.getActiveTokenCount()).isEqualTo(0);
    }

    @Test
    void clearExpiredTokens_ShouldRemoveExpiredTokens() {
        // This test just ensures the method doesn't throw an exception
        // In a real scenario, you'd need to manipulate time to test expiry
        passwordResetService.generateResetToken("testuser");
        
        passwordResetService.clearExpiredTokens();
        
        // Token should still be valid since it was just created
        assertThat(passwordResetService.getActiveTokenCount()).isEqualTo(1);
    }

    @Test
    void generateResetToken_WithSameUserTwice_ShouldCreateTwoTokens() {
        String username = "testuser";
        
        String token1 = passwordResetService.generateResetToken(username);
        String token2 = passwordResetService.generateResetToken(username);
        
        assertThat(token1).isNotEqualTo(token2);
        assertThat(passwordResetService.getActiveTokenCount()).isEqualTo(2);
        assertThat(passwordResetService.validateResetToken(token1)).isEqualTo(username);
        assertThat(passwordResetService.validateResetToken(token2)).isEqualTo(username);
    }
}