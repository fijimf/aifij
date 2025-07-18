package com.fijimf.deepfij.auth.util;

import com.fijimf.deepfij.service.TokenBlacklistService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Base64;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secretKey;
    
    @Value("${app.jwt.expiration:1800000}") // Default 30 minutes
    private int expirationTime;
    
    @Value("${app.jwt.refresh-expiration:604800000}") // Default 7 days
    private int refreshExpirationTime;
    
    private final TokenBlacklistService tokenBlacklistService;
    
    @Autowired
    public JwtUtil(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    private Key getSigningKey() {
        try {
            // Try to decode base64 secret key
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            // If Base64 decoding fails, treat the secret as plain text
            // This handles cases where JWT_SECRET env var is not properly base64 encoded
            byte[] keyBytes = secretKey.getBytes();
            if (keyBytes.length < 32) {
                // HMAC-SHA256 requires at least 32 bytes (256 bits)
                throw new IllegalArgumentException("JWT secret key must be at least 32 bytes when not base64 encoded");
            }
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, String username) {
        try {
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                return false;
            }
            String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    public void invalidateToken(String token) {
        tokenBlacklistService.blacklistToken(token);
    }

    private boolean isTokenExpired(String token) {
        Date expirationDate = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expirationDate.before(new Date());
    }
}