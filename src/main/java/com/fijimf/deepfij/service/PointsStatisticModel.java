package com.fijimf.deepfij.service;

import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repo.GameRepository;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PointsStatisticModel implements StatisticalModel {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private StatisticTypeService statisticTypeService;

    @Override
    public String key() {
        return "POINTS";
    }

    @Override
    public List<TeamStatistic> generate(Season season) {

        StatisticType pointsForAvg = statisticTypeService.findOrCreateStatisticType("POINTS_FOR_AVG", "POINTS_FOR_AVG", "Average points for", true, 2);
        StatisticType pointsForStdDev = statisticTypeService.findOrCreateStatisticType("POINTS_FOR_SD", "POINTS_FOR_SD", "Standard deviation points for", false, 2);
        StatisticType pointsAgainstAvg = statisticTypeService.findOrCreateStatisticType("POINTS_AGAINST_AVG", "POINTS_AGAINST_AVG", "Average points against", false,2);
        StatisticType pointsAgainstStdDev = statisticTypeService.findOrCreateStatisticType("POINTS_AGAINST_SD", "POINTS_AGAINST_SD", "Standard deviation of points against", false,2);
        StatisticType marginAvg = statisticTypeService.findOrCreateStatisticType("MARGIN_AVG", "MARGIN_AVG", "Average margin", true,2);
        StatisticType marginStdDev = statisticTypeService.findOrCreateStatisticType("MARGIN_SD", "MARGIN_SD", "Standard deviation of margin", false,2);

        // Get all games for the season ordered by date
        List<Game> games = gameRepository.findBySeasonOrderByDateAsc(season);

        // Map to track running totals for each team
        Map<Team, List<Integer>> pointsFor = new HashMap<>();
        Map<Team, List<Integer>> pointsAgainst = new HashMap<>();
        Map<Team, List<Integer>> margins = new HashMap<>();

        List<TeamStatistic> statistics = new ArrayList<>();
        // Process each game and update running totals

        Map<LocalDate, List<Game>> gamesByDate = games.stream().filter(Game::isComplete).collect(Collectors.groupingBy(Game::getDate));
        LocalDate lastDate = gamesByDate.keySet().stream().max(LocalDate::compareTo).orElseThrow();
        LocalDate startDate = gamesByDate.keySet().stream().min(LocalDate::compareTo).orElseThrow();
        for (LocalDate date = startDate; !date.isAfter(lastDate); date = date.plusDays(1)) {
            List<Game> dayGames = gamesByDate.getOrDefault(date, Collections.emptyList());
            for (Game game : dayGames) {
                if (game.isComplete()) {
                    pointsFor.putIfAbsent(game.getHomeTeam(), new ArrayList<>());
                    pointsFor.get(game.getHomeTeam()).add(game.getHomeScore());
                    pointsAgainst.putIfAbsent(game.getHomeTeam(), new ArrayList<>());
                    pointsAgainst.get(game.getHomeTeam()).add(game.getAwayScore());
                    margins.putIfAbsent(game.getHomeTeam(), new ArrayList<>());
                    margins.get(game.getHomeTeam()).add(game.getHomeScore() - game.getAwayScore());

                    pointsFor.putIfAbsent(game.getAwayTeam(), new ArrayList<>());
                    pointsFor.get(game.getAwayTeam()).add(game.getAwayScore());
                    pointsAgainst.putIfAbsent(game.getAwayTeam(), new ArrayList<>());
                    pointsAgainst.get(game.getAwayTeam()).add(game.getHomeScore());
                    margins.putIfAbsent(game.getAwayTeam(), new ArrayList<>());
                    margins.get(game.getAwayTeam()).add(game.getAwayScore() - game.getHomeScore());
                }
            }

            for (Map.Entry<Team, List<Integer>> entry : pointsFor.entrySet()) {

                Team team = entry.getKey();
                TeamStatisticBuilder base = new TeamStatisticBuilder().withTeam(team).withSeason(season).withDate(date);
                double[] pf = StatsCalculator.calculateStats(pointsFor.get(team));
                double[] pa = StatsCalculator.calculateStats(pointsAgainst.get(team));
                double[] mrg = StatsCalculator.calculateStats(margins.get(team));
                statistics.add(base.withType(pointsForAvg).withValue(BigDecimal.valueOf(pf[0])).build());
                statistics.add(base.withType(pointsForStdDev).withValue(BigDecimal.valueOf(pf[1])).build());
                statistics.add(base.withType(pointsAgainstAvg).withValue(BigDecimal.valueOf(pa[0])).build());
                statistics.add(base.withType(pointsAgainstStdDev).withValue(BigDecimal.valueOf(pa[1])).build());
                statistics.add(base.withType(marginAvg).withValue(BigDecimal.valueOf(mrg[0])).build());
                statistics.add(base.withType(marginStdDev).withValue(BigDecimal.valueOf(mrg[1])).build());
            }
        }
        return statistics;
    }

    public static class StatsCalculator {
        public static double[] calculateStats(List<Integer> numbers) {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            numbers.forEach(stats::addValue);

            return new double[]{
                    stats.getMean(),
                    stats.getStandardDeviation()
            };
        }
    }

}
