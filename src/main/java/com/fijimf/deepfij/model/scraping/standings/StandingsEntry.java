package com.fijimf.deepfij.model.scraping.standings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fijimf.deepfij.model.scraping.team.Team;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StandingsEntry(Team team, List<Stat> stats) {
}