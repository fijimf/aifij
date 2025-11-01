package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.statistics.StatisticSummary;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.time.LocalDate;

public record SummaryStub(LocalDate date, int n, double min, double q1, double med, double q3, double max, double mean,
                          double stdDev,
                          double percentile95, double percentile99, double skewness, double kurtosis

) {
    public static SummaryStub fromDescriptiveStatistics(LocalDate date, DescriptiveStatistics d) {
        return new SummaryStub(date, (int) d.getN(), d.getMin(), d.getPercentile(25.0), d.getPercentile(50.0), d.getPercentile(75.0), d.getMax(), d.getMean(), d.getStandardDeviation(),
                d.getPercentile(95.0), d.getPercentile(99.0), d.getSkewness(), d.getKurtosis());
    }

    /**
     * Creates a SummaryStub from a StatisticSummary domain object.
     * Converts BigDecimal values to doubles for DTO representation.
     *
     * @param summary The StatisticSummary domain object
     * @return A new SummaryStub instance
     */
    public static SummaryStub fromStatisticSummary(StatisticSummary summary) {
        return new SummaryStub(
            summary.date(),
            summary.count(),
            summary.minimum().doubleValue(),
            summary.firstQuartile().doubleValue(),
            summary.median().doubleValue(),
            summary.thirdQuartile().doubleValue(),
            summary.maximum().doubleValue(),
            summary.mean().doubleValue(),
            summary.standardDeviation().doubleValue(),
            summary.percentile95().doubleValue(),
            summary.percentile99().doubleValue(),
            summary.skewness().doubleValue(),
            summary.kurtosis().doubleValue()
        );
    }

    public static SummaryStub nullStub(LocalDate date) {
        return new SummaryStub(date, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }
}
