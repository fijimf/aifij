package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fijimf.deepfij.model.schedule.Team;

import java.util.List;

// Represents a Team (individual teams inside leagues)
@JsonIgnoreProperties(ignoreUnknown = true)
public record RawTeam(
        String id,
        String uid,
        String slug,
        String abbreviation,
        @JsonProperty("displayName") String displayName,
        @JsonProperty("shortDisplayName") String shortDisplayName,
        String name,
        String nickname,
        String location,
        @JsonProperty("color") String primaryColor,
        @JsonProperty("alternateColor") String alternateColor,
        boolean isActive,
        @JsonProperty("logos") List<Logo> logos
) {

    public Team getTeam() {
        Team team = new Team();
        team.setId(0L);
        team.setEspnId(this.id());
        team.setPrimaryColor(this.primaryColor());
        team.setSecondaryColor( this.alternateColor());
        team.setAbbreviation(this.abbreviation());
        team.setNickname(this.name());
        team.setName(this.nickname());
        team.setLongName(this.location());
        team.setSlug(this.slug());
        this.logos().stream().filter(l->l.rel().contains("primary_logo_on_white_color")).forEach(l->team.setLogoUrl(l.href()));
        return team;
    }
}
