package com.fijimf.deepfij.model.scraping.standings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fijimf.deepfij.model.scraping.team.RawTeam;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StandingsEntry(@JsonProperty("team") RawTeam rawTeam, List<Stat> stats) {
}