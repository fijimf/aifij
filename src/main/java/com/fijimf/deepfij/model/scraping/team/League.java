package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Represents a League (e.g., NCAA Men's Basketball)
@JsonIgnoreProperties(ignoreUnknown = true)
public class League {
    private String id;
    private String uid;
    private String name;
    private String abbreviation;
    private String shortName;
    private String slug;

    @JsonProperty("teams")
    private List<TeamWrapper> teams;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public List<TeamWrapper> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamWrapper> teams) {
        this.teams = teams;
    }
}
