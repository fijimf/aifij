package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Root model representing all the data
@JsonIgnoreProperties(ignoreUnknown = true)
public record SportsResponse(
        @JsonProperty("sports") List<Sport> sports
) {
}
