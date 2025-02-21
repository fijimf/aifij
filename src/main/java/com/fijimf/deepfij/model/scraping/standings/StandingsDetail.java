package com.fijimf.deepfij.model.scraping.standings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StandingsDetail(
        String id,
        String name,
        String displayName,
        int season,
        int seasonType,
        String seasonDisplayName,
        List<StandingsEntry> entries
) {
}