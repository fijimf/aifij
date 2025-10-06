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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NormalizedPointsStatisticModel implements StatisticalModel {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private StatisticTypeService statisticTypeService;

    @Override
    public String key() {
        return "NORMALIZED_POINTS";
    }

    public static final String NORM_PF_AVG_DESCRIPTION = """
            Normalized Points For Average is a team's scoring ability adjusted for opponent defensive strength.
            Each game's points scored is normalized by subtracting the opponent's points-against mean and dividing by their points-against standard deviation.
            This metric accounts for the quality of defenses faced.""";

    public static final String NORM_PA_AVG_DESCRIPTION = """
            Normalized Points Against Average is a team's defensive ability adjusted for opponent offensive strength.
            Each game's points allowed is normalized by subtracting the opponent's points-for mean and dividing by their points-for standard deviation.
            This metric accounts for the quality of offenses faced.""";

    public static final String NORM_PF_AVG_KEY = "NORM_PF_AVG";
    public static final String NORM_PA_AVG_KEY = "NORM_PA_AVG";

    @Override
    public List<StatisticType> refreshDBTypes() {
        return List.of(
                statisticTypeService.findOrCreateStatisticType(NORM_PF_AVG_KEY, "NORM_PF_AVG", NORM_PF_AVG_DESCRIPTION, true, 4, key()),
                statisticTypeService.findOrCreateStatisticType(NORM_PA_AVG_KEY, "NORM_PA_AVG", NORM_PA_AVG_DESCRIPTION, false, 4, key())
        );
    }

    @Override
    public List<TeamStatistic> generate(Season season) {
        StatisticType normPfAvgType = statisticTypeService.findStatisticType(NORM_PF_AVG_KEY);
        StatisticType normPaAvgType = statisticTypeService.findStatisticType(NORM_PA_AVG_KEY);

        // Get all games for the season ordered by date
        List<Game> games = gameRepository.findBySeasonOrderByDateAsc(season);

        // Track raw points for calculating running means and standard deviations
        Map<Team, List<Integer>> pointsFor = new HashMap<>();
        Map<Team, List<Integer>> pointsAgainst = new HashMap<>();

        // Track normalized scores
        Map<Team, List<Double>> normalizedPointsFor = new HashMap<>();
        Map<Team, List<Double>> normalizedPointsAgainst = new HashMap<>();

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
                    int homeScore = game.getHomeScore();
                    int awayScore = game.getAwayScore();

                    // Calculate normalized scores for this game
                    // Normalize home team's points for by away team's points against stats
                    double homeNormPf = normalizeScore(homeScore, pointsAgainst.get(awayTeam));
                    // Normalize home team's points against by away team's points for stats
                    double homeNormPa = normalizeScore(awayScore, pointsFor.get(awayTeam));

                    // Normalize away team's points for by home team's points against stats
                    double awayNormPf = normalizeScore(awayScore, pointsAgainst.get(homeTeam));
                    // Normalize away team's points against by home team's points for stats
                    double awayNormPa = normalizeScore(homeScore, pointsFor.get(homeTeam));

                    // Update raw points
                    pointsFor.computeIfAbsent(homeTeam, k -> new ArrayList<>()).add(homeScore);
                    pointsAgainst.computeIfAbsent(homeTeam, k -> new ArrayList<>()).add(awayScore);
                    pointsFor.computeIfAbsent(awayTeam, k -> new ArrayList<>()).add(awayScore);
                    pointsAgainst.computeIfAbsent(awayTeam, k -> new ArrayList<>()).add(homeScore);

                    // Update normalized scores
                    normalizedPointsFor.computeIfAbsent(homeTeam, k -> new ArrayList<>()).add(homeNormPf);
                    normalizedPointsAgainst.computeIfAbsent(homeTeam, k -> new ArrayList<>()).add(homeNormPa);
                    normalizedPointsFor.computeIfAbsent(awayTeam, k -> new ArrayList<>()).add(awayNormPf);
                    normalizedPointsAgainst.computeIfAbsent(awayTeam, k -> new ArrayList<>()).add(awayNormPa);
                }
            }

            // Calculate statistics for all teams that have played
            for (Map.Entry<Team, List<Double>> entry : normalizedPointsFor.entrySet()) {
                Team team = entry.getKey();
                TeamStatisticBuilder base = new TeamStatisticBuilder()
                        .withTeam(team)
                        .withSeason(season)
                        .withDate(date);

                double normPfAvg = calculateMean(normalizedPointsFor.get(team));
                double normPaAvg = calculateMean(normalizedPointsAgainst.get(team));

                statistics.add(base.withType(normPfAvgType).withValue(BigDecimal.valueOf(normPfAvg)).build());
                statistics.add(base.withType(normPaAvgType).withValue(BigDecimal.valueOf(normPaAvg)).build());
            }
        }

        return statistics;
    }

    /**
     * Normalize a score by an opponent's historical statistics.
     * Returns (score - opponentMean) / opponentStdDev
     * If opponent has no history, returns 0.0 (neutral)
     */
    private double normalizeScore(int score, List<Integer> opponentHistory) {
        if (opponentHistory == null || opponentHistory.isEmpty()) {
            return 0.0; // No history to normalize against
        }

        DescriptiveStatistics stats = new DescriptiveStatistics();
        opponentHistory.forEach(stats::addValue);

        double mean = stats.getMean();
        double stdDev = stats.getStandardDeviation();

        // If stdDev is 0 or very small, avoid division by zero
        if (stdDev < 0.001) {
            return score - mean; // Return raw difference if no variance
        }

        return (score - mean) / stdDev;
    }

    /**
     * Calculate the mean of a list of doubles
     */
    private double calculateMean(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}
