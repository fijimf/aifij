package com.fijimf.deepfij.service;

import java.time.LocalDate;
import java.util.List;

import com.fijimf.deepfij.model.statistics.StatisticSummary;
import com.fijimf.deepfij.model.statistics.TeamStatistic;

public interface StatisticService {
    /**
     * Retrieves a time series of statistical summaries for a given season and statistic type.
     * Each summary contains count, min, Q1, median, Q3, max, mean, and standard deviation.
     *
     * @param seasonId The ID of the season
     * @param statisticTypeName The name of the statistic type
     * @return List of StatisticSummary objects ordered by date
     */
    List<StatisticSummary> getStatisticSummariesBySeasonAndType(Long seasonId, String statisticTypeName);

    /**
     * Retrieves the top N teams for a given date, season, and statistic type.
     * The ordering is determined by the isHigherBetter flag in the StatisticType.
     *
     * @param seasonId The ID of the season
     * @param statisticTypeName The name of the statistic type
     * @param date The date to get statistics for
     * @param limit The maximum number of teams to return
     * @return List of TeamStatistic objects ordered by value (descending if higher is better, ascending if lower is better)
     */
    List<TeamStatistic> getTopTeamsByDate(Long seasonId, String statisticTypeName, LocalDate date, int limit);
} 