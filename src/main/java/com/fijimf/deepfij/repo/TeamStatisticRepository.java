package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.model.statistics.StatisticType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.statistics.TeamStatistic;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface TeamStatisticRepository extends JpaRepository<TeamStatistic, Long>, TeamStatisticRepositoryCustom {

    @Modifying
    @Query(value = """
            INSERT INTO team_statistic (team_id, season_id, statistic_date, statistic_type_id, numeric_value, last_updated_at)
            VALUES (:teamId, :seasonId, :statisticDate, :statisticTypeId, :numericValue, :lastUpdatedAt)
            ON CONFLICT (team_id, season_id, statistic_date, statistic_type_id)
            DO UPDATE SET
                numeric_value = EXCLUDED.numeric_value,
                last_updated_at = EXCLUDED.last_updated_at
            """, nativeQuery = true)
    void upsertTeamStatistic(@Param("teamId") Long teamId,
                            @Param("seasonId") Long seasonId,
                            @Param("statisticDate") LocalDate statisticDate,
                            @Param("statisticTypeId") Long statisticTypeId,
                            @Param("numericValue") BigDecimal numericValue,
                            @Param("lastUpdatedAt") ZonedDateTime lastUpdatedAt);

    void deleteBySeason(Season s);
    List<TeamStatistic> findBySeasonIdAndStatisticTypeId(Long seasonId, Long statisticTypeId);

    List<TeamStatistic> findBySeasonIdAndStatisticTypeIdAndStatisticDate(
            Long seasonId, Long statisticTypeId, LocalDate statisticDate);

    List<TeamStatistic> findByTeamAndStatisticTypeAndSeason(Team team, StatisticType statisticType, Season season);

    List<TeamStatistic> findByStatisticTypeAndSeason(StatisticType statisticType, Season season);

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

    @Query(value = "select s.year, st.model_key, count(*) count from team_statistic ts\n" +
            "inner join season s on s.id = ts.season_id\n" +
            "inner join statistic_type st on st.id = ts.statistic_type_id\n" +
            "group by s.year, st.model_key", nativeQuery = true)
    List<Map<String,Object>> countTeamsBySeasonAndModel();

    @Query(value = "select ts.statistic_date date, ts.team_id, ts.numeric_value \"value\" from team_statistic ts\n" +
            "inner join statistic_type st on st.id = ts.statistic_type_id\n" +
            "where st.name = ?1",
            nativeQuery = true)
    List<Map<String,Object>> loadAllStatisticsForModel(String stat);

    @Query(value = "SELECT * FROM team_statistic " +
            "WHERE season_id = :seasonId " +
            "AND statistic_type_id = :statisticTypeId " +
            "AND statistic_date = (SELECT MAX(statistic_date) " +
            "                      FROM team_statistic " +
            "                      WHERE season_id = :seasonId " +
            "                      AND statistic_type_id = :statisticTypeId " +
            "                      AND statistic_date < :beforeDate) " +
            "ORDER BY numeric_value DESC",
            nativeQuery = true)
    List<TeamStatistic> findLatestBeforeDate(@Param("seasonId") Long seasonId,
                                             @Param("statisticTypeId") Long statisticTypeId,
                                             @Param("beforeDate") LocalDate beforeDate);
    @Query(value = "SELECT * FROM team_statistic " +
            "WHERE season_id = :seasonId " +
            "AND statistic_type_id = :statisticTypeId " +
            "AND team_id = :teamId " +
            "AND statistic_date = (SELECT MAX(statistic_date) " +
            "                      FROM team_statistic " +
            "                      WHERE season_id = :seasonId " +
            "                      AND statistic_type_id = :statisticTypeId " +
            "                      AND statistic_date < :beforeDate) " +
            "ORDER BY numeric_value DESC",
            nativeQuery = true)

    List<TeamStatistic> findForTeamLatestBeforeDate(@Param("teamId") Long teamId,
                                                    @Param("seasonId") Long seasonId,
                                             @Param("statisticTypeId") Long statisticTypeId,
                                             @Param("beforeDate") LocalDate beforeDate);

}
