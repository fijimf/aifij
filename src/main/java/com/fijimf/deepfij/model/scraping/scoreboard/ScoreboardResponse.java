package com.fijimf.deepfij.model.scraping.scoreboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScoreboardResponse(
        List<Sport> sports
) {
    public List<Event> events() {
        if (sports == null) {
            return Collections.emptyList();
        } else {
            return sports()
                    .stream()
                    .map(Sport::events)
                    .reduce(new ArrayList<>(), (a, b) -> {
                        a.addAll(b);
                        return a;
                    });
        }
    }
}