package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.schedule.Game;

import java.time.LocalDate;

public record GameDTO(long id, int season, LocalDate date, TeamDTO homeTeam, TeamDTO awayTeam, Integer homeTeamSeed, Integer awayTeamSeed, Integer homeScore,
                      Integer awayScore, boolean isNeutral, ConferenceDTO conferenceDTO, String spread, Double overUnder, Integer homeMoneyLine, Integer awayMoneyLine) {
    public static GameDTO fromGame(Game game) {
        return new GameDTO(game.getId(),
                game.getSeason().getYear(),
                game.getDate(),
                TeamDTO.fromTeam(game.getHomeTeam()),
                TeamDTO.fromTeam(game.getAwayTeam()),
                game.getHomeTeamSeed(),
                game.getAwayTeamSeed(),
                game.getHomeScore(),
                game.getAwayScore(),
        game.isNeutralGame(), game.isConferenceGame()?ConferenceDTO.fromConference(game.getHomeTeamConference(),null):null,
                game.getSpreadDescription(),
                game.getOverUnder(),
                game.getHomeMoneyLine(),
                game.getAwayMoneyLine()
        );
    }
}
