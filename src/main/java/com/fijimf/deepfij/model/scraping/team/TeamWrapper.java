package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fijimf.deepfij.model.schedule.Team;


// Wrapper for Team object (data in the "team" field)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamWrapper(@JsonProperty("team") RawTeam rawTeam) {
    public Team toTeam(){
        return this.rawTeam.getTeam();
    }

    public void updateTeam(Team team){
        this.rawTeam.updateTeam(team);
    }


}

