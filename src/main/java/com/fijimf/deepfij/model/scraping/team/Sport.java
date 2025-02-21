package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// Represents a specific Sport (e.g., basketball)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Sport(
        String id,
        String uid,
        String name,
        String slug,
        List<League> leagues
) {
}
