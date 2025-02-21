package com.fijimf.deepfij.model.scraping.standings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@JsonIgnoreProperties(ignoreUnknown = true)
public record ConferenceStanding(
        String uid,
        String id,
        String name,
        String abbreviation,
        String shortName,
        boolean isConference,
        StandingsDetail standings,
        List<ConferenceStanding> children
) {
    public List<StandingsEntry> consolidatedStandings() {
        if (standings == null && children != null) {
            return children.stream().flatMap(c -> c.consolidatedStandings().stream()).collect(Collectors.toList());
        } else if (standings != null && children == null) {
            return standings.entries();
        } else {
            return Collections.emptyList();
        }
    }
}