package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.schedule.Game;

import java.time.LocalDate;

public record GameDTO(long id, int season, LocalDate date, TeamDTO homeTeam, TeamDTO awayTeam, int homeScore,
                      int awayScore) {
    public static GameDTO fromGame(Game game) {
        return new GameDTO(game.getId(),
                game.getSeason().getYear(),
                game.getDate(),
                TeamDTO.fromTeam(game.getHomeTeam()),
                TeamDTO.fromTeam(game.getAwayTeam()),
                game.getHomeScore(),
                game.getAwayScore());
    }
}
