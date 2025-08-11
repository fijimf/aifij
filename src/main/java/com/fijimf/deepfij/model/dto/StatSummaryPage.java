package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public record StatSummaryPage(
        String key,
        String name,
        String description,
        int seasonYear,
        LocalDate asOf,
        boolean isHigherBetter,
        int decimalPlaces,
        SummaryStub asOfSummary,
        List<TeamStatisticStub> statistics,
        List< SummaryStub> summaryStubs) {
    public static StatSummaryPage create(Season season, LocalDate asOf, StatisticType statisticType, List<TeamStatistic> teamStatistics, SortedMap<LocalDate, DescriptiveStatistics> descriptiveStatsByDate) {
        List<TeamStatisticStub> teamStatisticStubs = teamStatistics.stream().map(teamStatistic -> TeamStatisticStub.fromTeamStatistic(teamStatistic, teamStatistics.indexOf(teamStatistic)+1)).toList();
        List<SummaryStub> summaryStubs= descriptiveStatsByDate.entrySet().stream().map(e->SummaryStub.fromDescriptiveStatistics(e.getKey(),e.getValue())).sorted(Comparator.comparing(SummaryStub::date)).toList();


        return new StatSummaryPage(
                statisticType.getModelKey(),
                statisticType.getName(),
                statisticType.getDescription(),
                season.getYear(),
                asOf,
                statisticType.getIsHigherBetter(),
                statisticType.getDecimalPlaces(),
                summaryStubs.stream().filter(s->s.date().isEqual(asOf)).findFirst().orElseThrow(),
                correctRanks(teamStatisticStubs, statisticType.getIsHigherBetter()),
                summaryStubs);
    }

    private static List<TeamStatisticStub> correctRanks(List<TeamStatisticStub> teamStatisticStubs, boolean isHigherBetter) {
        Map<BigDecimal, List<TeamStatisticStub>> map = teamStatisticStubs.stream().collect(Collectors.groupingBy(TeamStatisticStub::statisticValue));
        SortedMap<BigDecimal, List<TeamStatisticStub>> sortedMap = new TreeMap<>((a,b)->a.compareTo(b)* (isHigherBetter?-1:1));
        sortedMap.putAll(map);
        List<TeamStatisticStub> response = new ArrayList<>();

        sortedMap.forEach((key1, value) -> {
            int rankTiesBest = response.size() + 1;
            double rankTiesAvg = response.size() + (value.size() + 1.0) / 2.0;
            value.stream().sorted(Comparator.comparing(stub -> stub.team().name())).forEach(e -> {
                response.add(e.withRank(response.size() + 1).withRankTieBest(rankTiesBest).withRankTieAvg(rankTiesAvg));
            });
        });
        return response;
    }
}


