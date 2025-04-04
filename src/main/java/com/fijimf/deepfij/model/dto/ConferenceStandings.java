package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.schedule.Conference;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Record;
import com.fijimf.deepfij.model.schedule.Team;

import java.util.List;
import java.util.Set;

public record ConferenceStandings (Conference conference, List<StandingsRow> standings) {
    public static ConferenceStandings create(Conference conference, Set<Team> teams, List<Game> games) {
        List<Game> conferenceGames = games.stream()
                .filter(g->teams.contains(g.getHomeTeam()) && teams.contains(g.getAwayTeam()))
                .toList();
        List<StandingsRow> standingsRows = teams.stream().map(team -> {
            com.fijimf.deepfij.model.schedule.Record conferenceRecord = com.fijimf.deepfij.model.schedule.Record.create(conferenceGames, team);
            com.fijimf.deepfij.model.schedule.Record overallRecord = Record.create(games, team);
            return new StandingsRow(team, conferenceRecord, overallRecord);
        }).sorted().toList();
        return new ConferenceStandings(conference, standingsRows);
    }
}
