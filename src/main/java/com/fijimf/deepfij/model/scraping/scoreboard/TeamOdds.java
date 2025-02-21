package com.fijimf.deepfij.model.scraping.scoreboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamOdds(
    int moneyLine
) {} 