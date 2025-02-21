package com.fijimf.deepfij.model.scraping.standings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StandingsResponse(
        String uid,
        String id,
        String name,
        String abbreviation,
        String shortName,
        List<ConferenceStanding> children,
        boolean isConference
) {
}