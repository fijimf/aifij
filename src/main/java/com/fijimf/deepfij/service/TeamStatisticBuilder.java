package com.fijimf.deepfij.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.model.statistics.TeamStatistic;

// Create a TeamStatisticBuilder class
public class TeamStatisticBuilder {
    private Team team;
    private Season season;
    private LocalDate date;
    private StatisticType type;
    private BigDecimal value;
    
    public TeamStatisticBuilder withTeam(Team team) {
        this.team = team;
        return this;
    }
    
    public TeamStatisticBuilder withSeason(Season season) {
        this.season = season;
        return this;
    }
    
    public TeamStatisticBuilder withDate(LocalDate date) {
        this.date = date;
        return this;
    }
    
    public TeamStatisticBuilder withType(StatisticType type) {
        this.type = type;
        return this;
    }
    
    public TeamStatisticBuilder withValue(BigDecimal value) {
        this.value = value;
        return this;
    }
    
    public TeamStatistic build() {
        TeamStatistic statistic = new TeamStatistic();
        statistic.setTeam(team);
        statistic.setSeason(season);
        statistic.setStatisticDate(date);
        statistic.setStatisticType(type);
        statistic.setNumericValue(value);
        statistic.setLastUpdatedAt(ZonedDateTime.now());
        return statistic;
    }
}