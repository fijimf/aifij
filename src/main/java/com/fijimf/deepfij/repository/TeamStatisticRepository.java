package com.fijimf.deepfij.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fijimf.deepfij.model.statistics.TeamStatistic;

@Repository
public interface TeamStatisticRepository extends JpaRepository<TeamStatistic, Long> {
    List<TeamStatistic> findBySeasonIdAndStatisticTypeId(Long seasonId, Long statisticTypeId);
} 