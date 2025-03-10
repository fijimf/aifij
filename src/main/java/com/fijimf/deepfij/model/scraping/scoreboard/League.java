package com.fijimf.deepfij.model.scraping.scoreboard;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record League(
    String id,
    String uid,
    String name,
    String abbreviation,
    String shortName,
    String slug,
    String tag,
    boolean isTournament,
    List<String> smartdates,
    List<Event> events
) {
    public List<Event> eventList() {
        if (events == null) {return Collections.emptyList(); } else { return events;}
    }
}