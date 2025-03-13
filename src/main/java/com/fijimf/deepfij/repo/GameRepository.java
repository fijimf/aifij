package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findBySeasonOrderByDateAsc(Season s);

    List<Game> findBySeasonAndDate(Season s, LocalDate d);

    List<Game> findBySeasonAndIndexDate(Season s, LocalDate d);
}
