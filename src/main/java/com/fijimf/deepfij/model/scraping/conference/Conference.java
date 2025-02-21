package com.fijimf.deepfij.model.scraping.conference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Defines a Conference Object to map JSON data
@JsonIgnoreProperties(ignoreUnknown = true)
public record Conference(
        String uid,

        @JsonProperty("groupId") String groupId,
        @JsonProperty("name") String name,
        @JsonProperty("shortName") String shortName,
        @JsonProperty("logo") String logo,
        @JsonProperty("parentGroupId") String parentGroupId,
        @JsonProperty("subGroups") List<String> subGroups
) {
}
