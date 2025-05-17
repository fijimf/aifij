package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.statistics.StatisticSummary;
import com.fijimf.deepfij.model.statistics.TeamStatistic;

import java.util.List;

public record StatSummaryPage(
    String name,
    String description,
    boolean isHigherBetter,
    int decimalPlaces,
    int season,
    List<TeamStatisticStub> statistics,
    List<StatisticSummary> summaries){}

