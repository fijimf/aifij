package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fijimf.deepfij.model.schedule.Team;

import java.util.Optional;


// Wrapper for Team object (data in the "team" field)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamWrapper(@JsonProperty("team") RawTeam rawTeam) {
    public Team toTeam(){
        RawTeam rawTeam = this.rawTeam;
        Team team = new Team();
        team.setId(0L);
        team.setEspnId(rawTeam.id());
        team.setPrimaryColor(rawTeam.primaryColor());
        team.setSecondaryColor( rawTeam.alternateColor());
        team.setAbbreviation(rawTeam.abbreviation());
        team.setNickname(rawTeam.name());
        team.setName(rawTeam.nickname());
        team.setLongName(rawTeam.location());
        team.setSlug(rawTeam.slug());
        rawTeam.logos().stream().filter(l->l.rel().contains("primary_logo_on_white_color")).forEach(l->team.setLogoUrl(l.href()));
        return team;
    }
}

