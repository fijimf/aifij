package com.fijimf.deepfij.model.scraping.conference;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ConferenceResponse(@JsonProperty("conferences") List<RawConference> conferences) {
}
