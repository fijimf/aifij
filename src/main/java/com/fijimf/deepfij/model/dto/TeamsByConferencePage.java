package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.schedule.*;
import com.fijimf.deepfij.model.schedule.Record;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record TeamsByConferencePage(List<ConferenceTeams> conferences) {

    public static TeamsByConferencePage create(List<Team> teams, Season season) {
        Map<Team, Conference> conferenceMap = season.getConferenceMappings().stream()
                .collect(Collectors.toMap(ConferenceMapping::getTeam, ConferenceMapping::getConference));

        Map<Conference, List<Team>> teamsByConference = teams.stream()
                .filter(conferenceMap::containsKey)
                .collect(Collectors.groupingBy(conferenceMap::get));

        List<ConferenceTeams> conferenceTeamsList = teamsByConference.entrySet().stream()
                .map(entry -> {
                    Conference conference = entry.getKey();
                    List<Team> conferenceTeams = entry.getValue();
                    
                    List<TeamStub> teamStubs = conferenceTeams.stream()
                            .map(team -> {
                                List<Game> conferenceGames = season.getGames().stream()
                                        .filter(game -> game.isInConference(team, conferenceMap))
                                        .toList();
                                Record conferenceRecord = Record.create(conferenceGames, team);
                                Record overallRecord = Record.create(season.getGames(), team);
                                
                                return new TeamStub(
                                        team.getId(),
                                        team.getName(),
                                        team.getNickname(),
                                        team.getLogoUrl(),
                                        conference.getName(),
                                        conference.getLogoUrl(),
                                        conference.getId(),
                                        overallRecord,
                                        conferenceRecord
                                );
                            })
                            .sorted((t1, t2) -> Record.NATURAL_ORDER.reversed().compare(t1.conferenceRecord(), t2.conferenceRecord()))
                            .toList();

                    return new ConferenceTeams(
                            conference.getId(),
                            conference.getName(),
                            conference.getLogoUrl(),
                            teamStubs
                    );
                })
                .sorted((c1, c2) -> c1.name().compareTo(c2.name()))
                .toList();

        return new TeamsByConferencePage(conferenceTeamsList);
    }

    public record ConferenceTeams(
            long id,
            String name,
            String logoUrl,
            List<TeamStub> teams
    ) {}

    public record TeamStub(
            long id,
            String name,
            String nickname,
            String logoUrl,
            String conference,
            String conferenceLogoUrl,
            long conferenceId,
            Record overallRecord,
            Record conferenceRecord
    ) {}
}