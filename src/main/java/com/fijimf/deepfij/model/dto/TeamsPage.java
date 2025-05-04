package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.schedule.*;
import com.fijimf.deepfij.model.schedule.Record;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record TeamsPage (List<TeamStub> teams) {


    public static TeamsPage create(List<Team> teams, Season season) {
        Map<Team, Conference> conferenceMap = season.getConferenceMappings().stream().collect(Collectors.toMap(ConferenceMapping::getTeam, ConferenceMapping::getConference));

        return new TeamsPage(teams.stream().map(team -> new TeamStub(
                team.getId(),
                team.getName(),
                team.getNickname(),
                team.getLogoUrl(),
                conferenceMap.containsKey(team)?conferenceMap.get(team).getName():null,
                conferenceMap.containsKey(team)?conferenceMap.get(team).getLogoUrl():null,
                conferenceMap.containsKey(team)?conferenceMap.get(team).getId():-1,
                Record.create(season.getGames(), team)
        )).toList());
    }

    record TeamStub(
            long id,
            String name,
            String nickname,
            String logoUrl,
            String conference,
            String conferenceLogoUrl,
            long conferenceId,
            Record record
    ) { }
}
