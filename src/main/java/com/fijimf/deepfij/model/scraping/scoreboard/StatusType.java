package com.fijimf.deepfij.model.scraping.scoreboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StatusType(
    String id,
    String name,
    String state,
    boolean completed,
    String description,
    String detail,
    String shortDetail
) {} 