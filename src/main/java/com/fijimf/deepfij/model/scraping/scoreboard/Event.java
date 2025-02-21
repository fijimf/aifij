package com.fijimf.deepfij.model.scraping.scoreboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.ZonedDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Event(
    String id,
    String uid,
    String guid,
    ZonedDateTime date,
    boolean timeValid,
    boolean recent,
    String name,
    String shortName,
    boolean gamecastAvailable,
    boolean playByPlayAvailable,
    boolean commentaryAvailable,
    String location,
    String status,
    String summary,
    int period,
    String clock,
    String note,
    List<Competitor> competitors,
    boolean neutralSite,
    Odds odds,
    FullStatus fullStatus
) {} 