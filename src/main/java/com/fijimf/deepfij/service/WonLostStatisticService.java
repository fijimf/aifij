package com.fijimf.deepfij.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
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
import com.fijimf.deepfij.repo.StatisticTypeRepository;

@Service
public class WonLostStatisticService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private StatisticTypeRepository statisticTypeRepository;

    @Autowired
    private StatisticUtils statisticUtils;

    public List<TeamStatistic> createWonLostStatistic(Season season) {
        StatisticType winsType = statisticTypeRepository.findByCode("WINS")
                .orElseGet(() -> statisticUtils.createStatisticType("WINS", "WINS", "Wins", true));
        StatisticType lossesType = statisticTypeRepository.findByCode("LOSSES")
                .orElseGet(() -> statisticUtils.createStatisticType("LOSSES", "LOSSES", "Losses", false));
        StatisticType winStreakType = statisticTypeRepository.findByCode("WIN_STREAK")
                .orElseGet(() -> statisticUtils.createStatisticType("WIN_STREAK", "WIN_STREAK", "Winning Streak", true));
        StatisticType lossStreakType = statisticTypeRepository.findByCode("LOSS_STREAK")
                .orElseGet(() -> statisticUtils.createStatisticType("LOSS_STREAK", "LOSS_STREAK", "Losing Streak", false));
        StatisticType winningPctType = statisticTypeRepository.findByCode("WIN_PCT")
                .orElseGet(() -> statisticUtils.createStatisticType("WIN_PCT", "WIN_PCT", "Winning Pct", true));

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

            // Create statistics for all teams for this date
            for (Map.Entry<Team, Integer> entry : wins.entrySet()) {
                Team team = entry.getKey();
                
                TeamStatistic winsStat = new TeamStatistic();
                winsStat.setTeam(team);
                winsStat.setSeason(season);
                winsStat.setStatisticDate(date);
                winsStat.setStatisticType(winsType);
                winsStat.setNumericValue(BigDecimal.valueOf(wins.get(team)));
                winsStat.setLastUpdatedAt(ZonedDateTime.now());
                statistics.add(winsStat);

                TeamStatistic lossesStat = new TeamStatistic();
                lossesStat.setTeam(team);
                lossesStat.setSeason(season);
                lossesStat.setStatisticDate(date);
                lossesStat.setStatisticType(lossesType);
                lossesStat.setNumericValue(BigDecimal.valueOf(losses.get(team)));
                lossesStat.setLastUpdatedAt(ZonedDateTime.now());
                statistics.add(lossesStat);

                TeamStatistic winStreakStat = new TeamStatistic();
                winStreakStat.setTeam(team);
                winStreakStat.setSeason(season);
                winStreakStat.setStatisticDate(date);
                winStreakStat.setStatisticType(winStreakType);
                winStreakStat.setNumericValue(BigDecimal.valueOf(winStreak.get(team)));
                winStreakStat.setLastUpdatedAt(ZonedDateTime.now());
                statistics.add(winStreakStat);

                TeamStatistic lossStreakStat = new TeamStatistic();
                lossStreakStat.setTeam(team);
                lossStreakStat.setSeason(season);
                lossStreakStat.setStatisticDate(date);
                lossStreakStat.setStatisticType(lossStreakType);
                lossStreakStat.setNumericValue(BigDecimal.valueOf(lossStreak.get(team)));
                lossStreakStat.setLastUpdatedAt(ZonedDateTime.now());
                statistics.add(lossStreakStat);

                TeamStatistic winningPctStat = new TeamStatistic();
                winningPctStat.setTeam(team);
                winningPctStat.setSeason(season);
                winningPctStat.setStatisticDate(date);
                winningPctStat.setStatisticType(winningPctType);
                winningPctStat.setNumericValue(BigDecimal.valueOf(winningPct.get(team)));
                winningPctStat.setLastUpdatedAt(ZonedDateTime.now());
                statistics.add(winningPctStat);
            }
        }
        return statistics;
    }
}
