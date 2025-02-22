package com.fijimf.deepfij.model.scraping.standings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fijimf.deepfij.model.scraping.team.RawTeam;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StandingsEntry(RawTeam rawTeam, List<Stat> stats) {
}