package com.fijimf.deepfij.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fijimf.deepfij.model.statistics.StatisticSummary;
import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repository.StatisticTypeRepository;
import com.fijimf.deepfij.repository.TeamStatisticRepository;
import com.fijimf.deepfij.service.StatisticService;

@Service
public class StatisticServiceImpl implements StatisticService {

    @Autowired
    private TeamStatisticRepository teamStatisticRepository;

    @Autowired
    private StatisticTypeRepository statisticTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StatisticSummary> getStatisticSummariesBySeasonAndType(Long seasonId, String statisticTypeName) {
        StatisticType statisticType = statisticTypeRepository.findByName(statisticTypeName)
                .orElseThrow(() -> new IllegalArgumentException("Statistic type not found: " + statisticTypeName));

        List<TeamStatistic> statistics = teamStatisticRepository.findBySeasonIdAndStatisticTypeId(
                seasonId, statisticType.getId());

        // Group statistics by date
        Map<LocalDate, List<TeamStatistic>> statisticsByDate = statistics.stream()
                .collect(Collectors.groupingBy(TeamStatistic::getStatisticDate));

        // Calculate summary statistics for each date
        return statisticsByDate.entrySet().stream()
                .map(entry -> calculateSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(StatisticSummary::date))
                .collect(Collectors.toList());
    }

    private StatisticSummary calculateSummary(LocalDate date, List<TeamStatistic> statistics) {
        List<BigDecimal> values = statistics.stream()
                .map(TeamStatistic::getNumericValue)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        if (values.isEmpty()) {
            throw new IllegalArgumentException("No valid statistics found for date: " + date);
        }

        int count = values.size();
        BigDecimal min = values.get(0);
        BigDecimal max = values.get(count - 1);
        
        // Calculate quartiles
        BigDecimal q1 = calculateQuartile(values, 0.25);
        BigDecimal median = calculateQuartile(values, 0.5);
        BigDecimal q3 = calculateQuartile(values, 0.75);

        // Calculate mean
        BigDecimal sum = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mean = sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);

        // Calculate standard deviation
        BigDecimal variance = values.stream()
                .map(value -> value.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()))
                .setScale(4, RoundingMode.HALF_UP);

        return new StatisticSummary(date, count, min, q1, median, q3, max, mean, stdDev);
    }

    private BigDecimal calculateQuartile(List<BigDecimal> sortedValues, double percentile) {
        int size = sortedValues.size();
        double position = (size - 1) * percentile;
        int lowerIndex = (int) Math.floor(position);
        int upperIndex = (int) Math.ceil(position);
        
        if (lowerIndex == upperIndex) {
            return sortedValues.get(lowerIndex);
        }
        
        BigDecimal lowerValue = sortedValues.get(lowerIndex);
        BigDecimal upperValue = sortedValues.get(upperIndex);
        double weight = position - lowerIndex;
        
        return lowerValue.add(upperValue.subtract(lowerValue).multiply(BigDecimal.valueOf(weight)))
                .setScale(4, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamStatistic> getTopTeamsByDate(Long seasonId, String statisticTypeName, LocalDate date, int limit) {
        StatisticType statisticType = statisticTypeRepository.findByName(statisticTypeName)
                .orElseThrow(() -> new IllegalArgumentException("Statistic type not found: " + statisticTypeName));

        List<TeamStatistic> statistics = teamStatisticRepository.findBySeasonIdAndStatisticTypeIdAndStatisticDate(
                seasonId, statisticType.getId(), date);

        return statistics.stream()
                .filter(stat -> stat.getNumericValue() != null)
                .sorted((a, b) -> {
                    int comparison = a.getNumericValue().compareTo(b.getNumericValue());
                    return Boolean.TRUE.equals(statisticType.getIsHigherBetter()) ? 
                            -comparison : // Higher is better, so reverse the comparison
                            comparison;   // Lower is better, so keep the comparison
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
} 