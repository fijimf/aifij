package com.fijimf.deepfij.model.scraping.conference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Defines a Conference Object to map JSON data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Conference {
    private String uid;

    @JsonProperty("groupId")
    private String groupId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("shortName")
    private String shortName;

    @JsonProperty("logo")
    private String logo;

    @JsonProperty("parentGroupId")
    private String parentGroupId;

    @JsonProperty("subGroups")
    private List<String> subGroups;

    // Getters and setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getParentGroupId() {
        return parentGroupId;
    }

    public void setParentGroupId(String parentGroupId) {
        this.parentGroupId = parentGroupId;
    }

    public List<String> getSubGroups() {
        return subGroups;
    }

    public void setSubGroups(List<String> subGroups) {
        this.subGroups = subGroups;
    }

    @Override
    public String toString() {
        return "Conference{" +
                "uid='" + uid + '\'' +
                ", groupId='" + groupId + '\'' +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", logo='" + logo + '\'' +
                ", parentGroupId='" + parentGroupId + '\'' +
                ", subGroups=" + subGroups +
                '}';
    }
}
