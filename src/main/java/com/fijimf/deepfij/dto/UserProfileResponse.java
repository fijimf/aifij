package com.fijimf.deepfij.dto;

import java.util.Set;

public record UserProfileResponse(
    Long id,
    String username,
    boolean enabled,
    Set<String> roles
) {
}