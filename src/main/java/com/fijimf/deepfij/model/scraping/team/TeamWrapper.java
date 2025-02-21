package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


// Wrapper for Team object (data in the "team" field)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamWrapper(@JsonProperty("team") Team team) {
}

