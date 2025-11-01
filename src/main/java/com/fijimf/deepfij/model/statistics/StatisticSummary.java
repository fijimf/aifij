package com.fijimf.deepfij.model.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record StatisticSummary(
    LocalDate date,
    int count,
    BigDecimal minimum,
    BigDecimal firstQuartile,
    BigDecimal median,
    BigDecimal thirdQuartile,
    BigDecimal maximum,
    BigDecimal mean,
    BigDecimal standardDeviation,
    BigDecimal percentile95,
    BigDecimal percentile99,
    BigDecimal skewness,
    BigDecimal kurtosis
) {
    public StatisticSummary {
        if (date == null) throw new IllegalArgumentException("Date cannot be null");
        if (count < 0) throw new IllegalArgumentException("Count cannot be negative");
        if (minimum == null) throw new IllegalArgumentException("Minimum cannot be null");
        if (firstQuartile == null) throw new IllegalArgumentException("First quartile cannot be null");
        if (median == null) throw new IllegalArgumentException("Median cannot be null");
        if (thirdQuartile == null) throw new IllegalArgumentException("Third quartile cannot be null");
        if (maximum == null) throw new IllegalArgumentException("Maximum cannot be null");
        if (mean == null) throw new IllegalArgumentException("Mean cannot be null");
        if (standardDeviation == null) throw new IllegalArgumentException("Standard deviation cannot be null");
        if (percentile95 == null) throw new IllegalArgumentException("Percentile 95 cannot be null");
        if (percentile99 == null) throw new IllegalArgumentException("Percentile 99 cannot be null");
        if (skewness == null) throw new IllegalArgumentException("Skewness cannot be null");
        if (kurtosis == null) throw new IllegalArgumentException("Kurtosis cannot be null");
    }

    /**
     * Creates a StatisticSummary from Apache Commons Math DescriptiveStatistics.
     *
     * @param date The date for this summary
     * @param stats The DescriptiveStatistics object containing calculated statistics
     * @return A new StatisticSummary instance
     */
    public static StatisticSummary fromDescriptiveStatistics(LocalDate date, DescriptiveStatistics stats) {
        return new StatisticSummary(
            date,
            (int) stats.getN(),
            toBigDecimal(stats.getMin()),
            toBigDecimal(stats.getPercentile(25.0)),
            toBigDecimal(stats.getPercentile(50.0)),
            toBigDecimal(stats.getPercentile(75.0)),
            toBigDecimal(stats.getMax()),
            toBigDecimal(stats.getMean()),
            toBigDecimal(stats.getStandardDeviation()),
            toBigDecimal(stats.getPercentile(95.0)),
            toBigDecimal(stats.getPercentile(99.0)),
            toBigDecimal(stats.getSkewness()),
            toBigDecimal(stats.getKurtosis())
        );
    }

    /**
     * Creates a null/empty StatisticSummary with all values set to zero.
     *
     * @param date The date for this summary
     * @return A new StatisticSummary instance with zero values
     */
    public static StatisticSummary nullSummary(LocalDate date) {
        return new StatisticSummary(
            date,
            0,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        );
    }

    private static BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
    }
} 