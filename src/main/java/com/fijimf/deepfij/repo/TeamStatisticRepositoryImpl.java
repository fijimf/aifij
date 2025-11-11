package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.statistics.TeamStatistic;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class TeamStatisticRepositoryImpl implements TeamStatisticRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    public TeamStatisticRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void batchUpsert(List<TeamStatistic> statistics) {
        String sql = """
            INSERT INTO team_statistic (team_id, season_id, statistic_date, statistic_type_id, numeric_value, last_updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (team_id, season_id, statistic_date, statistic_type_id)
            DO UPDATE SET
                numeric_value = EXCLUDED.numeric_value,
                last_updated_at = EXCLUDED.last_updated_at
            """;

        jdbcTemplate.batchUpdate(sql, statistics, statistics.size(),
            (PreparedStatement ps, TeamStatistic stat) -> {
                ps.setLong(1, stat.getTeam().getId());
                ps.setLong(2, stat.getSeason().getId());
                ps.setObject(3, stat.getStatisticDate());
                ps.setLong(4, stat.getStatisticType().getId());
                ps.setBigDecimal(5, stat.getNumericValue());
                ps.setTimestamp(6, Timestamp.from(stat.getLastUpdatedAt().toInstant()));
            });
    }
}
