package com.fijimf.deepfij.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final ConcurrentMap<String, PasswordResetToken> resetTokens = new ConcurrentHashMap<>();

    public String generateResetToken(String username) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1); // 1 hour expiry
        resetTokens.put(token, new PasswordResetToken(username, expiry));
        return token;
    }

    public String validateResetToken(String token) {
        PasswordResetToken resetToken = resetTokens.get(token);
        if (resetToken == null) {
            return null;
        }
        
        if (resetToken.expiry().isBefore(LocalDateTime.now())) {
            resetTokens.remove(token);
            return null;
        }
        
        return resetToken.username();
    }

    public void consumeResetToken(String token) {
        resetTokens.remove(token);
    }

    public void clearExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        resetTokens.entrySet().removeIf(entry -> 
            entry.getValue().expiry().isBefore(now)
        );
    }

    public int getActiveTokenCount() {
        return resetTokens.size();
    }

    public void clearAll() {
        resetTokens.clear();
    }

    public record PasswordResetToken(String username, LocalDateTime expiry) {
    }
}