package com.fijimf.deepfij.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;

import com.fijimf.deepfij.model.schedule.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Season;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findBySeasonOrderByDateAsc(Season s);

    List<Game> findBySeasonAndIndexDate(Season s, LocalDate d);

    long deleteBySeason(Season s);

    @Query("SELECT g FROM Game g WHERE g.season = ?1 AND g.homeTeamSeed IS NOT NULL AND g.awayTeamSeed IS NOT NULL ORDER BY g.date DESC")
    List<Game> findTournamentGamesBySeason(Season season);

    List<Game> findBySeasonYear(int year);

    @Query("SELECT MAX(g.indexDate) FROM Game g WHERE g.season = ?1 AND g.homeScore IS NOT NULL AND g.awayScore IS NOT NULL")
    LocalDate findLastCompletedGameDate(Season season);

    long countBySeasonEquals(Season season);

    @Query("SELECT g FROM Game g WHERE g.season = :season AND " +
            "((g.homeTeam = :team1 AND g.awayTeam = :team2) OR (g.homeTeam = :team2 AND g.awayTeam = :team1)) AND " +
            "g.date < :beforeDate ORDER BY g.date DESC")
    List<Game> findBetweenTeamsInSeason(@Param("team1") Team team1, @Param("team2") Team team2,
                                         @Param("season") Season season, @Param("beforeDate") LocalDate beforeDate);

    @Query("SELECT g FROM Game g WHERE " +
            "((g.homeTeam = :team1 AND g.awayTeam = :team2) OR (g.homeTeam = :team2 AND g.awayTeam = :team1)) AND " +
            "g.date >= :afterDate AND g.date < :beforeDate ORDER BY g.date DESC")
    List<Game> findBetweenTeams(@Param("team1") Team team1, @Param("team2") Team team2,
                                 @Param("afterDate") LocalDate afterDate, @Param("beforeDate") LocalDate beforeDate);

    @Query("SELECT g FROM Game g WHERE g.season = :season AND " +
            "(g.homeTeam = :team OR g.awayTeam = :team) AND g.date < :beforeDate ORDER BY g.date DESC")
    List<Game> findByTeamAndSeasonBeforeDate(@Param("team") Team team, @Param("season") Season season,
                                              @Param("beforeDate") LocalDate beforeDate);

}
