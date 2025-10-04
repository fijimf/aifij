package com.fijimf.deepfij.service.stat;

import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repo.GameRepository;
import com.fijimf.deepfij.service.StatisticTypeService;
import com.fijimf.deepfij.service.StatisticalModel;
import com.fijimf.deepfij.service.TeamStatisticBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RpiStatisticModel implements StatisticalModel {

    private static final double WP_WEIGHT = 0.25;
    private static final double OWP_WEIGHT = 0.50;
    private static final double OOWP_WEIGHT = 0.25;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private StatisticTypeService statisticTypeService;

    @Override
    public String key() {
        return "RPI";
    }

    public static final String RPI_DESCRIPTION = """
            Rating Percentage Index (RPI) is a composite metric that combines a team's winning percentage (25%),
            their opponents' winning percentage (50%), and their opponents' opponents' winning percentage (25%).
            This metric rewards teams for both winning games and playing against strong competition.""";

    public static final String RPI_WP_DESCRIPTION = """
            RPI Winning Percentage component represents a team's own winning percentage.
            This is the team's wins divided by total games played, weighted at 25% in the RPI calculation.""";

    public static final String RPI_OWP_DESCRIPTION = """
            RPI Opponents' Winning Percentage (OWP) is the average winning percentage of all opponents faced.
            This measures strength of schedule, weighted at 50% in the RPI calculation.""";

    public static final String RPI_OOWP_DESCRIPTION = """
            RPI Opponents' Opponents' Winning Percentage (OOWP) is the average winning percentage of all opponents' opponents.
            This provides a secondary measure of schedule strength, weighted at 25% in the RPI calculation.""";

    public static final String RPI_KEY = "RPI";
    public static final String RPI_WP_KEY = "RPI_WP";
    public static final String RPI_OWP_KEY = "RPI_OWP";
    public static final String RPI_OOWP_KEY = "RPI_OOWP";

    @Override
    public List<StatisticType> refreshDBTypes() {
        return List.of(
                statisticTypeService.findOrCreateStatisticType(RPI_KEY, "RPI", RPI_DESCRIPTION, true, 4, key()),
                statisticTypeService.findOrCreateStatisticType(RPI_WP_KEY, "RPI_WP", RPI_WP_DESCRIPTION, true, 4, key()),
                statisticTypeService.findOrCreateStatisticType(RPI_OWP_KEY, "RPI_OWP", RPI_OWP_DESCRIPTION, true, 4, key()),
                statisticTypeService.findOrCreateStatisticType(RPI_OOWP_KEY, "RPI_OOWP", RPI_OOWP_DESCRIPTION, true, 4, key())
        );
    }

    @Override
    public List<TeamStatistic> generate(Season season) {
        StatisticType rpiType = statisticTypeService.findStatisticType(RPI_KEY);
        StatisticType rpiWpType = statisticTypeService.findStatisticType(RPI_WP_KEY);
        StatisticType rpiOwpType = statisticTypeService.findStatisticType(RPI_OWP_KEY);
        StatisticType rpiOowpType = statisticTypeService.findStatisticType(RPI_OOWP_KEY);

        // Get all games for the season ordered by date
        List<Game> games = gameRepository.findBySeasonOrderByDateAsc(season);

        // Track wins and losses for each team
        Map<Team, Integer> wins = new HashMap<>();
        Map<Team, Integer> losses = new HashMap<>();
        Map<Team, List<Team>> opponents = new HashMap<>();
        List<TeamStatistic> statistics = new ArrayList<>();

        // Process games by date
        Map<LocalDate, List<Game>> gamesByDate = games.stream()
                .filter(Game::isComplete)
                .collect(Collectors.groupingBy(Game::getDate));

        LocalDate lastDate = gamesByDate.keySet().stream().max(LocalDate::compareTo).orElseThrow();
        LocalDate startDate = gamesByDate.keySet().stream().min(LocalDate::compareTo).orElseThrow();

        for (LocalDate date = startDate; !date.isAfter(lastDate); date = date.plusDays(1)) {
            List<Game> dayGames = gamesByDate.getOrDefault(date, Collections.emptyList());

            for (Game game : dayGames) {
                if (game.isComplete()) {
                    Team homeTeam = game.getHomeTeam();
                    Team awayTeam = game.getAwayTeam();
                    Team winner;
                    Team loser;

                    if (game.getHomeScore() > game.getAwayScore()) {
                        winner = homeTeam;
                        loser = awayTeam;
                    } else {
                        winner = awayTeam;
                        loser = homeTeam;
                    }

                    // Update wins and losses
                    wins.put(winner, wins.getOrDefault(winner, 0) + 1);
                    wins.putIfAbsent(loser, 0);
                    losses.put(loser, losses.getOrDefault(loser, 0) + 1);
                    losses.putIfAbsent(winner, 0);

                    // Track opponents
                    opponents.computeIfAbsent(homeTeam, k -> new ArrayList<>()).add(awayTeam);
                    opponents.computeIfAbsent(awayTeam, k -> new ArrayList<>()).add(homeTeam);
                }
            }

            // Calculate RPI components for all teams that have played
            for (Team team : wins.keySet()) {
                double wp = calculateWinningPercentage(team, wins, losses);
                double owp = calculateOpponentsWinningPercentage(team, opponents, wins, losses);
                double oowp = calculateOpponentsOpponentsWinningPercentage(team, opponents, wins, losses);
                double rpi = (WP_WEIGHT * wp) + (OWP_WEIGHT * owp) + (OOWP_WEIGHT * oowp);

                TeamStatisticBuilder base = new TeamStatisticBuilder()
                        .withTeam(team)
                        .withSeason(season)
                        .withDate(date);

                statistics.add(base.withType(rpiType).withValue(BigDecimal.valueOf(rpi)).build());
                statistics.add(base.withType(rpiWpType).withValue(BigDecimal.valueOf(wp)).build());
                statistics.add(base.withType(rpiOwpType).withValue(BigDecimal.valueOf(owp)).build());
                statistics.add(base.withType(rpiOowpType).withValue(BigDecimal.valueOf(oowp)).build());
            }
        }

        return statistics;
    }

    private double calculateWinningPercentage(Team team, Map<Team, Integer> wins, Map<Team, Integer> losses) {
        int teamWins = wins.getOrDefault(team, 0);
        int teamLosses = losses.getOrDefault(team, 0);
        int totalGames = teamWins + teamLosses;

        return totalGames > 0 ? (double) teamWins / totalGames : 0.0;
    }

    private double calculateOpponentsWinningPercentage(Team team, Map<Team, List<Team>> opponents,
                                                       Map<Team, Integer> wins, Map<Team, Integer> losses) {
        List<Team> teamOpponents = opponents.getOrDefault(team, Collections.emptyList());

        if (teamOpponents.isEmpty()) {
            return 0.0;
        }

        double totalWp = 0.0;
        for (Team opponent : teamOpponents) {
            totalWp += calculateWinningPercentage(opponent, wins, losses);
        }

        return totalWp / teamOpponents.size();
    }

    private double calculateOpponentsOpponentsWinningPercentage(Team team, Map<Team, List<Team>> opponents,
                                                                Map<Team, Integer> wins, Map<Team, Integer> losses) {
        List<Team> teamOpponents = opponents.getOrDefault(team, Collections.emptyList());

        if (teamOpponents.isEmpty()) {
            return 0.0;
        }

        double totalOowp = 0.0;
        int opponentCount = 0;

        for (Team opponent : teamOpponents) {
            List<Team> opponentsOpponents = opponents.getOrDefault(opponent, Collections.emptyList());

            for (Team opponentOpponent : opponentsOpponents) {
                totalOowp += calculateWinningPercentage(opponentOpponent, wins, losses);
                opponentCount++;
            }
        }

        return opponentCount > 0 ? totalOowp / opponentCount : 0.0;
    }
}
