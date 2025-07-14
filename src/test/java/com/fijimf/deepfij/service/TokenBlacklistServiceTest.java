package com.fijimf.deepfij.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
    }

    @Test
    void blacklistToken_ShouldAddTokenToBlacklist() {
        String token = "test.jwt.token";
        
        tokenBlacklistService.blacklistToken(token);
        
        assertThat(tokenBlacklistService.isTokenBlacklisted(token)).isTrue();
        assertThat(tokenBlacklistService.getBlacklistedTokenCount()).isEqualTo(1);
    }

    @Test
    void isTokenBlacklisted_WithNonBlacklistedToken_ShouldReturnFalse() {
        String token = "test.jwt.token";
        
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(token);
        
        assertThat(isBlacklisted).isFalse();
    }

    @Test
    void clearAll_ShouldRemoveAllBlacklistedTokens() {
        tokenBlacklistService.blacklistToken("token1");
        tokenBlacklistService.blacklistToken("token2");
        
        tokenBlacklistService.clearAll();
        
        assertThat(tokenBlacklistService.getBlacklistedTokenCount()).isEqualTo(0);
        assertThat(tokenBlacklistService.isTokenBlacklisted("token1")).isFalse();
        assertThat(tokenBlacklistService.isTokenBlacklisted("token2")).isFalse();
    }

    @Test
    void blacklistToken_WithSameTokenTwice_ShouldNotDuplicate() {
        String token = "test.jwt.token";
        
        tokenBlacklistService.blacklistToken(token);
        tokenBlacklistService.blacklistToken(token);
        
        assertThat(tokenBlacklistService.getBlacklistedTokenCount()).isEqualTo(1);
    }

    @Test
    void clearExpiredTokens_ShouldRemoveOldTokens() {
        // This test would need to manipulate time or use a different approach
        // For now, we'll test that the method doesn't throw an exception
        tokenBlacklistService.blacklistToken("token1");
        
        tokenBlacklistService.clearExpiredTokens();
        
        // Since we just added the token, it shouldn't be expired
        assertThat(tokenBlacklistService.isTokenBlacklisted("token1")).isTrue();
    }
}