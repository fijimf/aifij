package com.fijimf.deepfij.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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


}
