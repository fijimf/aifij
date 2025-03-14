package com.fijimf.deepfij.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Season;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findBySeasonOrderByDateAsc(Season s);

    List<Game> findBySeasonAndIndexDate(Season s, LocalDate d);
}
