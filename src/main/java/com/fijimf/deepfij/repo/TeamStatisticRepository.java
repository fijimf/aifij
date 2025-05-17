package com.fijimf.deepfij.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.statistics.TeamStatistic;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TeamStatisticRepository extends JpaRepository<TeamStatistic, Long> {

    void deleteBySeason(Season s);
    List<TeamStatistic> findBySeasonIdAndStatisticTypeId(Long seasonId, Long statisticTypeId);

    List<TeamStatistic> findBySeasonIdAndStatisticTypeIdAndStatisticDate(
            Long seasonId, Long statisticTypeId, LocalDate statisticDate);

}
