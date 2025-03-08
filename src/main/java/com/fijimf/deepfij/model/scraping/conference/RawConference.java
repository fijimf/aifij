package com.fijimf.deepfij.model.scraping.conference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Defines a Conference Object to map JSON data
@JsonIgnoreProperties(ignoreUnknown = true)
public record RawConference(
        String uid,

        @JsonProperty("groupId") String groupId,
        @JsonProperty("name") String name,
        @JsonProperty("shortName") String shortName,
        @JsonProperty("logo") String logo,
        @JsonProperty("parentGroupId") String parentGroupId,
        @JsonProperty("subGroups") List<String> subGroups
) {
    public boolean isConference() {
        return groupId != null && name != null && shortName != null && !shortName.equals("ALL");
    }

    public com.fijimf.deepfij.model.schedule.Conference toConference() {
        com.fijimf.deepfij.model.schedule.Conference conference = new com.fijimf.deepfij.model.schedule.Conference();
        conference.setId(0L);
        conference.setName(name);
        conference.setShortName(shortName);
        conference.setEspnId(groupId);
        conference.setLogoUrl(logo);
        return conference;
    }

    public void updateConference(com.fijimf.deepfij.model.schedule.Conference conference) {
        conference.setName(name);
        conference.setShortName(shortName);
        conference.setEspnId(groupId);
        conference.setLogoUrl(logo);
    }
}
