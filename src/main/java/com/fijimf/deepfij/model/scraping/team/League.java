package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Represents a League (e.g., NCAA Men's Basketball)
@JsonIgnoreProperties(ignoreUnknown = true)
public record League(
        String id,
        String uid,
        String name,
        String abbreviation,
        String shortName,
        String slug,
        @JsonProperty("teams")
        List<TeamWrapper> teams
) {
}
