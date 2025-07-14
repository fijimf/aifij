package com.fijimf.deepfij.auth.util;

import com.fijimf.deepfij.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString("test-secret-key-for-jwt-testing-that-is-long-enough".getBytes());
    private static final int TEST_EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        TokenBlacklistService blacklistService = new TokenBlacklistService();
        jwtUtil = new JwtUtil(blacklistService);
        ReflectionTestUtils.setField(jwtUtil, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", TEST_EXPIRATION);
    }

    @Test
    void generateToken_WithValidUsername_ShouldReturnToken() {
        String username = "testuser";
        
        String token = jwtUtil.generateToken(username);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts separated by dots
    }

    @Test
    void extractUsername_WithValidToken_ShouldReturnUsername() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        
        String extractedUsername = jwtUtil.extractUsername(token);
        
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        
        boolean isValid = jwtUtil.validateToken(token, username);
        
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_WithWrongUsername_ShouldReturnFalse() {
        String username = "testuser";
        String wrongUsername = "wronguser";
        String token = jwtUtil.generateToken(username);
        
        boolean isValid = jwtUtil.validateToken(token, wrongUsername);
        
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Set very short expiration time
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", 1); // 1ms expiration
        
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Reset to normal expiration for validation
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", TEST_EXPIRATION);
        
        boolean isValid = jwtUtil.validateToken(token, username);
        
        assertThat(isValid).isFalse();
    }

    @Test
    void extractUsername_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token.here";
        
        assertThatThrownBy(() -> jwtUtil.extractUsername(invalidToken))
            .isInstanceOf(Exception.class);
    }

    @Test
    void extractUsername_WithMalformedToken_ShouldThrowException() {
        String malformedToken = "not-a-jwt-token";
        
        assertThatThrownBy(() -> jwtUtil.extractUsername(malformedToken))
            .isInstanceOf(Exception.class);
    }

    @Test
    void generateToken_WithNullUsername_ShouldReturnTokenWithNullSubject() {
        String token = jwtUtil.generateToken(null);
        
        assertThat(token).isNotNull();
        assertThat(jwtUtil.extractUsername(token)).isNull();
    }

    @Test
    void generateToken_WithEmptyUsername_ShouldReturnToken() {
        String emptyUsername = "";
        
        String token = jwtUtil.generateToken(emptyUsername);
        
        assertThat(token).isNotNull();
        // JWT treats empty string as null in subject
        assertThat(jwtUtil.extractUsername(token)).isNull();
    }

    @Test
    void validateToken_SameTokenDifferentTimes_ShouldBeConsistent() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        
        boolean isValid1 = jwtUtil.validateToken(token, username);
        boolean isValid2 = jwtUtil.validateToken(token, username);
        
        assertThat(isValid1).isTrue();
        assertThat(isValid2).isTrue();
        assertThat(isValid1).isEqualTo(isValid2);
    }
}