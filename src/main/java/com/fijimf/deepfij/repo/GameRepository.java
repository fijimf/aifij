package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.schedule.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
}
