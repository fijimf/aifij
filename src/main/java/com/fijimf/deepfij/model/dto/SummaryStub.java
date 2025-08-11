package com.fijimf.deepfij.model.dto;

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
}
