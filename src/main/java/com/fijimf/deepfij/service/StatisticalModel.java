package com.fijimf.deepfij.service;

import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.model.statistics.TeamStatistic;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface StatisticalModel {

    String key();
    List<StatisticType>  refreshDBTypes();
    List<TeamStatistic> generate(Season season);

    default List<TeamStatistic> generate(Season season, List<LocalDate> dates) {

        Set<LocalDate> dateSet = new HashSet<>(dates);
        return generate(season)
                .stream()
                .filter(stat -> dateSet.contains(stat.getStatisticDate()))
                .collect(Collectors.toList());
    }
}
