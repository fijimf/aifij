package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.statistics.TeamStatistic;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TeamStatisticStub(int rank, int rankTieBest, double rankTieAvg, TeamDTO team, BigDecimal statisticValue,
                                LocalDate date) {
    public static TeamStatisticStub fromTeamStatistic(TeamStatistic teamStatistic, int rank) {
        return new TeamStatisticStub(rank, 0, 0.0, TeamDTO.fromTeam(teamStatistic.getTeam()), teamStatistic.getNumericValue(), teamStatistic.getStatisticDate());
    }

    public TeamStatisticStub withRank(int rank) {
        return new TeamStatisticStub(rank, this.rankTieBest, this.rankTieAvg, this.team, this.statisticValue, this.date);
    }

    public TeamStatisticStub withRankTieBest(int rankTieBest) {
        return new TeamStatisticStub(this.rank, rankTieBest, this.rankTieAvg, this.team, this.statisticValue, this.date);
    }

    public TeamStatisticStub withRankTieAvg(double rankTieAvg) {
        return new TeamStatisticStub(this.rank, this.rankTieBest, rankTieAvg, this.team, this.statisticValue, this.date);
    }
}
