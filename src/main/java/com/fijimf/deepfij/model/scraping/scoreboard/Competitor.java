package com.fijimf.deepfij.model.scraping.scoreboard;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Competitor(
    String id,
    String guid,
    String uid,
    String type,
    int order,
    String homeAway,
    boolean winner,
    String displayName,
    String name,
    String abbreviation,
    String location,
    String color,
    String alternateColor,
    String score,
    String record,
    List<Record> records,
    String logo,
    String logoDark,
    Integer rank,
    TournamentMatchup tournamentMatchup
) {} 