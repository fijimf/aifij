package com.fijimf.deepfij.model.scraping.scoreboard;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScoreboardResponse(
    List<Sport> sports
) {} 