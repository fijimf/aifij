package com.fijimf.deepfij.model.scraping.standings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Stat {
    private String name;
    private String displayName;
    private String shortDisplayName;
    private String description;
    private String abbreviation;
    private String type;
    private double value;
    private String displayValue;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public String getShortDisplayName() { return shortDisplayName; }
    public void setShortDisplayName(String shortDisplayName) { this.shortDisplayName = shortDisplayName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAbbreviation() { return abbreviation; }
    public void setAbbreviation(String abbreviation) { this.abbreviation = abbreviation; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    
    public String getDisplayValue() { return displayValue; }
    public void setDisplayValue(String displayValue) { this.displayValue = displayValue; }
} 