package com.fijimf.deepfij.service;

import java.util.List;

import com.fijimf.deepfij.model.statistics.StatisticSummary;

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
} 