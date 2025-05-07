package com.fijimf.deepfij.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repo.GameRepository;

@Service
public class WonLostStatisticModel implements StatisticalModel {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private StatisticTypeService statisticTypeService;
    @Override
    public String key() {
        return "WONLOST";
    }
    @Override
    public List<TeamStatistic> generate(Season season) {
        StatisticType winsType = statisticTypeService.findOrCreateStatisticType("WINS", "WINS", "Wins", true);
        StatisticType lossesType = statisticTypeService.findOrCreateStatisticType("LOSSES", "LOSSES", "Losses", false);
        StatisticType winStreakType = statisticTypeService.findOrCreateStatisticType("WIN_STREAK", "WIN_STREAK", "Winning Streak", true);
        StatisticType lossStreakType = statisticTypeService.findOrCreateStatisticType("LOSS_STREAK", "LOSS_STREAK", "Losing Streak", false);
        StatisticType winningPctType = statisticTypeService.findOrCreateStatisticType("WIN_PCT", "WIN_PCT", "Winning Pct", true);

        // Get all games for the season ordered by date
        List<Game> games = gameRepository.findBySeasonOrderByDateAsc(season);

        // Map to track running totals for each team
        Map<Team, Integer> wins = new HashMap<>();
        Map<Team, Integer> losses = new HashMap<>();
        Map<Team, Integer> winStreak = new HashMap<>();
        Map<Team, Integer> lossStreak = new HashMap<>();
        Map<Team, Double> winningPct = new HashMap<>();
        List<TeamStatistic> statistics = new ArrayList<>();
        // Process each game and update running totals

        Map<LocalDate, List<Game>> gamesByDate = games.stream().filter(Game::isComplete).collect(Collectors.groupingBy(Game::getDate));
        LocalDate lastDate = gamesByDate.keySet().stream().max(LocalDate::compareTo).orElseThrow();
        LocalDate startDate = gamesByDate.keySet().stream().min(LocalDate::compareTo).orElseThrow();
        for (LocalDate date = startDate; !date.isAfter(lastDate); date = date.plusDays(1)) {
            List<Game> dayGames = gamesByDate.getOrDefault(date, Collections.emptyList());
            for (Game game : dayGames) {
                if (game.isComplete()) {
                    Team winner;
                    Team loser;
                    if (game.getHomeScore() > game.getAwayScore()) {
                        winner = game.getHomeTeam();
                        loser = game.getAwayTeam();
                    } else {
                        winner = game.getAwayTeam();
                        loser = game.getHomeTeam();
                    }

                    wins.put(winner, wins.getOrDefault(winner, 0) + 1);
                    wins.putIfAbsent(loser, 0);
                    losses.put(loser, losses.getOrDefault(loser, 0) + 1);
                    losses.putIfAbsent(winner, 0);
                    winStreak.put(winner, winStreak.getOrDefault(winner, 0) + 1);
                    winStreak.put(loser, 0);
                    lossStreak.put(loser, lossStreak.getOrDefault(loser, 0) + 1);
                    lossStreak.put(winner, 0);
                   
                    winningPct.put(winner, (double)wins.get(winner)/(wins.get(winner)+losses.get(winner)));
                    winningPct.put(loser, (double)wins.get(loser)/(wins.get(loser)+losses.get(loser)));
                }
            }

            for (Map.Entry<Team, Integer> entry : wins.entrySet()) {
                Team team = entry.getKey();
                TeamStatisticBuilder base = new TeamStatisticBuilder().withTeam(team).withSeason(season).withDate(date);
                statistics.add(base.withType(winsType).withValue(BigDecimal.valueOf(wins.get(team))).build());
                statistics.add(base.withType(lossesType).withValue(BigDecimal.valueOf(losses.get(team))).build());
                statistics.add(base.withType(winStreakType).withValue(BigDecimal.valueOf(winStreak.get(team))).build());
                statistics.add(base.withType(lossStreakType).withValue(BigDecimal.valueOf(lossStreak.get(team))).build());
                statistics.add(base.withType(winningPctType).withValue(BigDecimal.valueOf(winningPct.get(team))).build());
            }
        }
        return statistics;
    }
}
