package com.fijimf.deepfij.model.scraping.standings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fijimf.deepfij.model.scraping.team.Team;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StandingsEntry {
    private Team team;
    private List<Stat> stats;

    // Getters and setters
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
    
    public List<Stat> getStats() { return stats; }
    public void setStats(List<Stat> stats) { this.stats = stats; }
} 