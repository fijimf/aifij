package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.statistics.TeamStatistic;

import java.util.List;

public interface TeamStatisticRepositoryCustom {
    void batchUpsert(List<TeamStatistic> statistics);
}
