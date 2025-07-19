package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.schedule.Game;

import java.time.LocalDate;

public record GameDTO(long id, int season, LocalDate date, TeamDTO homeTeam, TeamDTO awayTeam, Integer homeTeamSeed, Integer awayTeamSeed, int homeScore,
                      int awayScore, String round) {
    public static GameDTO fromGame(Game game, String round) {
        return new GameDTO(game.getId(),
                game.getSeason().getYear(),
                game.getDate(),
                TeamDTO.fromTeam(game.getHomeTeam()),
                TeamDTO.fromTeam(game.getAwayTeam()),
                game.getHomeTeamSeed(),
                game.getAwayTeamSeed(),
                game.getHomeScore(),
                game.getAwayScore(),
        round);
    }
}
