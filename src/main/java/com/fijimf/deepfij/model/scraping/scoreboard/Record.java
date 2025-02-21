package com.fijimf.deepfij.model.scraping.scoreboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Record(
    String type,
    String summary,
    String displayValue
) {} 