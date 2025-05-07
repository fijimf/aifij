package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamStatisticRepository extends JpaRepository<TeamStatistic, Long> {

}
