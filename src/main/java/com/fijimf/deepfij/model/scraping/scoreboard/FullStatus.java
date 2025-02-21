package com.fijimf.deepfij.model.scraping.scoreboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FullStatus(
    int clock,
    String displayClock,
    int period,
    StatusType type
) {} 