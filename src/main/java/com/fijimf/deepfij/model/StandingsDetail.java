package com.fijimf.deepfij.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StandingsDetail {
    private String id;
    private String name;
    private String displayName;
    private int season;
    private int seasonType;
    private String seasonDisplayName;
    private List<StandingsEntry> entries;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public int getSeason() { return season; }
    public void setSeason(int season) { this.season = season; }
    
    public int getSeasonType() { return seasonType; }
    public void setSeasonType(int seasonType) { this.seasonType = seasonType; }
    
    public String getSeasonDisplayName() { return seasonDisplayName; }
    public void setSeasonDisplayName(String seasonDisplayName) { this.seasonDisplayName = seasonDisplayName; }
    
    public List<StandingsEntry> getEntries() { return entries; }
    public void setEntries(List<StandingsEntry> entries) { this.entries = entries; }
} 