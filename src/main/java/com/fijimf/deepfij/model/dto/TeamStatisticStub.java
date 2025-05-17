package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.statistics.TeamStatistic;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TeamStatisticStub(int rank, TeamDTO team, BigDecimal statisticValue, LocalDate date) {
    public static TeamStatisticStub fromTeamStatistic(TeamStatistic teamStatistic, int rank) {
       return new TeamStatisticStub(rank, TeamDTO.fromTeam(teamStatistic.getTeam()), teamStatistic.getNumericValue(), teamStatistic.getStatisticDate());
    }
}
