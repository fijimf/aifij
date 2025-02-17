package com.fijimf.deepfij.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConferenceStanding {
    public static final Logger logger = LoggerFactory.getLogger(ConferenceStanding.class);
    private String uid;
    private String id;
    private String name;
    private String abbreviation;
    private String shortName;
    private boolean isConference;
    private StandingsDetail standings;
    private List<ConferenceStanding> children;

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
    
    public boolean isConference() { return isConference; }
    public void setConference(boolean conference) { isConference = conference; }
    
    public StandingsDetail getStandings() { return standings; }
    public void setStandings(StandingsDetail standings) { this.standings = standings; }

    public List<ConferenceStanding> getChildren() {
        return children;
    }

    public void setChildren(List<ConferenceStanding> children) {
        this.children = children;
    }

    public List<StandingsEntry> consolidatedStandings() {
        if (standings == null && children != null) {
            return children.stream().flatMap(c -> c.consolidatedStandings().stream()).collect(Collectors.toList());
        } else if (standings != null && children == null) {
            return standings. getEntries();
        } else {
            logger.warn("Conference " + getName() + " has no teams");
            return Collections.emptyList();
        }
    }
}