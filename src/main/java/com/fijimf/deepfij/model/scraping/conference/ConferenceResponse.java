package com.fijimf.deepfij.model.scraping.conference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Root Level Response Object
@JsonIgnoreProperties(ignoreUnknown = true) // Ignores any unknown fields in the JSON
public class ConferenceResponse {
    @JsonProperty("conferences")
    private List<Conference> conferences;

    public List<Conference> getConferences() {
        return conferences;
    }

    public void setConferences(List<Conference> conferences) {
        this.conferences = conferences;
    }
}
