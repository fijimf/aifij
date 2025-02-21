package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Represents a Team (individual teams inside leagues)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Team(
        String id,
        String uid,
        String slug,
        String abbreviation,
        @JsonProperty("displayName") String displayName,
        @JsonProperty("shortDisplayName") String shortDisplayName,
        String name,
        String nickname,
        String location,
        @JsonProperty("color") String primaryColor,
        @JsonProperty("alternateColor") String alternateColor,
        boolean isActive,
        @JsonProperty("logos") List<Logo> logos
) {
}
