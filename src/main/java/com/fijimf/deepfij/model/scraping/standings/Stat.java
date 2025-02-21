package com.fijimf.deepfij.model.scraping.standings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Stat(
        String name,
        String displayName,
        String shortDisplayName,
        String description,
        String abbreviation,
        String type,
        double value,
        String displayValue
) {
}