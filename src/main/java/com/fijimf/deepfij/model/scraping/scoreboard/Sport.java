package com.fijimf.deepfij.model.scraping.scoreboard;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Sport(
    String id,
    String uid,
    String guid,
    String name,
    String slug,
    List<Logo> logos,
    List<League> leagues
) {} 