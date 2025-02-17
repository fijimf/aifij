package com.fijimf.deepfij.service.scraping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Represents a Team (individual teams inside leagues)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Team {
    private String id;
    private String uid;
    private String slug;
    private String abbreviation;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("shortDisplayName")
    private String shortDisplayName;

    private String name;
    private String nickname;
    private String location;

    @JsonProperty("color")
    private String primaryColor;

    @JsonProperty("alternateColor")
    private String alternateColor;

    private boolean isActive;

    @JsonProperty("logos")
    private List<Logo> logos;

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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getShortDisplayName() {
        return shortDisplayName;
    }

    public void setShortDisplayName(String shortDisplayName) {
        this.shortDisplayName = shortDisplayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getAlternateColor() {
        return alternateColor;
    }

    public void setAlternateColor(String alternateColor) {
        this.alternateColor = alternateColor;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Logo> getLogos() {
        return logos;
    }

    public void setLogos(List<Logo> logos) {
        this.logos = logos;
    }
}
