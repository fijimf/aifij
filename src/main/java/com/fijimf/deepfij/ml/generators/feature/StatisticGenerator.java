package com.fijimf.deepfij.ml.generators.feature;

import com.fijimf.deepfij.ml.FeatureGenerator;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.repo.TeamStatisticRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.time.LocalDate;
import java.util.*;

public class StatisticGenerator implements FeatureGenerator {
    private final String key;
    private final SortedMap<LocalDate, Map<Long, Double>> statistics = new TreeMap<>();
    private final SortedMap<LocalDate, Double> means = new TreeMap<>();
    private final SortedMap<LocalDate, Double> stdDevs = new TreeMap<>();
    private final SortedMap<LocalDate, Double> min = new TreeMap<>();
    private final SortedMap<LocalDate, Double> max = new TreeMap<>();

    public StatisticGenerator(String key, TeamStatisticRepository repository) {
        this.key = key;


        repository.loadAllStatisticsForModel(key).forEach(teamStat -> {
            LocalDate date = ((java.sql.Date) teamStat.get("date")).toLocalDate();
            Double value = ((Number) teamStat.get("value")).doubleValue();
            statistics.computeIfAbsent(date, k -> new HashMap<>())
                    .put((Long) teamStat.get("team_id"), value);
        });
        statistics.forEach((date, teamStats) -> {
            DescriptiveStatistics stats = new DescriptiveStatistics(
                    teamStats.values().stream().mapToDouble(Double::doubleValue).toArray()
            );
            means.put(date, stats.getMean());
            stdDevs.put(date, stats.getStandardDeviation());
            min.put(date, stats.getMin());
            max.put(date, stats.getMax());
        });
    }

    public Map<String, Object> generateFeature(Game game,String homeAway, String normalize, int lookbackDays ) {
        if (!Set.of("home", "away").contains(homeAway)) {
            throw new IllegalArgumentException("Invalid homeAway value: " + homeAway + ". Must be 'home' or 'away'");
        }

        if (!(StringUtils.isBlank(normalize) || (Set.of("none", "z", "min-max").contains(normalize)))) {
            throw new IllegalArgumentException("Invalid normalize value: " + normalize + ". Must be 'none', 'z', or 'min-max'");
        }

        if (lookbackDays < 0) {
            throw new IllegalArgumentException("Invalid lookbackDays value: " + lookbackDays + ". Must be non-negative");
        }


        LocalDate lookbackDate = game.getDate().minusDays(lookbackDays);
        
        // Get the team based on homeAway parameter
        Long teamId = "home".equals(homeAway) ? game.getHomeTeam().getId() : game.getAwayTeam().getId();
        
        // Find the most recent statistics before the lookback date
        SortedMap<LocalDate, Map<Long, Double>> relevantStats = statistics.headMap(lookbackDate);
        if (relevantStats.isEmpty()) {
            throw new IllegalStateException("No statistics available before date: " + lookbackDate);
        }
        
        Map.Entry<LocalDate, Map<Long, Double>> lastEntry = relevantStats.lastEntry();
        LocalDate statsDate = lastEntry.getKey();
        Map<Long, Double> teamStatsForDate = lastEntry.getValue();
        
        Double value = teamStatsForDate.get(teamId);
        if (value == null) {
            throw new IllegalStateException("No statistic value found for team ID " + teamId + " on date " + statsDate);
        }
        
        // Apply normalization if specified
        if ("z".equals(normalize)) {
            Double mean = means.get(statsDate);
            Double stdDev = stdDevs.get(statsDate);
            if (mean == null || stdDev == null || stdDev == 0) {
                throw new IllegalStateException("Cannot normalize: missing or invalid mean/stddev for date " + statsDate);
            }
            value = (value - mean) / stdDev;
        } else if ("min-max".equals(normalize)) {
            Double minVal = min.get(statsDate);
            Double maxVal = max.get(statsDate);
            if (minVal == null || maxVal == null || maxVal.equals(minVal)) {
                throw new IllegalStateException("Cannot normalize: missing or invalid min/max for date " + statsDate);
            }
            value = (value - minVal) / (maxVal - minVal);
        }
        return Map.of(getKey(), value);
    }

    @Override
    public String getKey() {
        return key;
    }
}
