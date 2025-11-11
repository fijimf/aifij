package com.fijimf.deepfij.model.dto.gamedetail;

public record TeamStatistics(
        StatisticValue pointsForAvg,
        StatisticValue pointsForStdDev,
        StatisticValue pointsAgainstAvg,
        StatisticValue pointsAgainstStdDev,
        StatisticValue pointsCovariance,
        RankedStatistic linearRegression,
        RankedStatistic logisticRegression,
        RankedStatistic rpi
) {
}
