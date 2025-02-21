package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// Represents a Logo (Team logo images)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Logo(
        String href,
        String alt,
        List<String> rel,
        int width,
        int height
) {
}
