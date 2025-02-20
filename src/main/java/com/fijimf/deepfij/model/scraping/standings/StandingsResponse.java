package com.fijimf.deepfij.model.scraping.standings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StandingsResponse {
    private String uid;
    private String id;
    private String name;
    private String abbreviation;
    private String shortName;
    private List<ConferenceStanding> children;
    private boolean isConference;

    // Getters and setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAbbreviation() { return abbreviation; }
    public void setAbbreviation(String abbreviation) { this.abbreviation = abbreviation; }
    
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    
    public List<ConferenceStanding> getChildren() { return children; }
    public void setChildren(List<ConferenceStanding> children) { this.children = children; }
    
    public boolean isConference() { return isConference; }
    public void setConference(boolean conference) { isConference = conference; }
} 