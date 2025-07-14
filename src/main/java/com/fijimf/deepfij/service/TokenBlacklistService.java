package com.fijimf.deepfij.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TokenBlacklistService {

    private final ConcurrentMap<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String token) {
        blacklistedTokens.put(token, LocalDateTime.now());
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    public void clearExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        blacklistedTokens.entrySet().removeIf(entry -> 
            entry.getValue().isBefore(now.minusHours(24)) // Remove tokens older than 24 hours
        );
    }

    public int getBlacklistedTokenCount() {
        return blacklistedTokens.size();
    }

    public void clearAll() {
        blacklistedTokens.clear();
    }
}