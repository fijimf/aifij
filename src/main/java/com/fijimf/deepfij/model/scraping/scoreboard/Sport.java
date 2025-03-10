package com.fijimf.deepfij.model.scraping.scoreboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Sport(
        String id,
        String uid,
        String guid,
        String name,
        String slug,
        List<Logo> logos,
        List<League> leagues
) {
    public List<Event> events() {
        if (leagues == null) {
            return Collections.emptyList();
        } else {
            return leagues.stream()
                    .map(League::eventList)
                    .reduce(new ArrayList<>(), (a, b) -> {
                        a.addAll(b);
                        return a;
                    });
        }
    }
}