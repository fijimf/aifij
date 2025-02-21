package com.fijimf.deepfij.model.scraping.scoreboard;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Logo(
    String href,
    String alt,
    List<String> rel,
    int width,
    int height
) {} 