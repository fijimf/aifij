package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Root model representing all the data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SportsResponse {
    @JsonProperty("sports")
    private List<Sport> sports;

    public List<Sport> getSports() {
        return sports;
    }

    public void setSports(List<Sport> sports) {
        this.sports = sports;
    }
}
