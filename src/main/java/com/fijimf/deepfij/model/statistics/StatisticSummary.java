package com.fijimf.deepfij.model.statistics;

import java.math.BigDecimal;
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
    BigDecimal standardDeviation
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
    }
} 