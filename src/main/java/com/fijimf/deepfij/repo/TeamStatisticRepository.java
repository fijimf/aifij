package com.fijimf.deepfij.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.statistics.TeamStatistic;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface TeamStatisticRepository extends JpaRepository<TeamStatistic, Long> {

    void deleteBySeason(Season s);
    List<TeamStatistic> findBySeasonIdAndStatisticTypeId(Long seasonId, Long statisticTypeId);

    List<TeamStatistic> findBySeasonIdAndStatisticTypeIdAndStatisticDate(
            Long seasonId, Long statisticTypeId, LocalDate statisticDate);

    @Query(value = "select model, stat, season, count(date) num_days, max(date) last_date, sum(n) total\n" +
            "from (select st.model_key model,\n" +
            "       st.code stat,\n" +
            "       s.year season,\n" +
            "       ts.statistic_date date,\n" +
            "       count(*) n\n" +
            "from team_statistic ts\n" +
            "    inner join statistic_type st on ts.statistic_type_id = st.id\n" +
            "    inner join season s on ts.season_id = s.id\n" +
            "group by st.model_key, st.code, s.year, ts.statistic_date order by ts.statistic_date) as summary\n" +
            "group by model, stat, season", nativeQuery = true)
    List<Map<String,Object>> findSummary();

}
