package com.fijimf.deepfij.model.scraping.scoreboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Odds(
    String details,
    double overUnder,
    double spread,
    int overOdds,
    int underOdds,
    TeamOdds home,
    TeamOdds away
) {} 