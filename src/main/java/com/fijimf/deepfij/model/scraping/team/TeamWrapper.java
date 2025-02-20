package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Wrapper for Team object (data in the "team" field)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamWrapper {
    @JsonProperty("team")
    private Team team;

    // Getter and setter
    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}

