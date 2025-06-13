package com.fijimf.deepfij.ml.generators.target;

import com.fijimf.deepfij.ml.TargetGenerator;
import com.fijimf.deepfij.model.schedule.Game;

import java.util.List;
import java.util.Map;

public class HomeTeamWonGenerator implements TargetGenerator {
    @Override
    public String getKey() {
        return "home-team-won";
    }

    @Override
    public List<String> getModelsSupported() {
        return List.of();
    }

    @Override
    public Map<String, Object> generateTarget(Game game) {
        return game.getHomeScore()>=game.getAwayScore() ? Map.of("home-team-won", 1) : Map.of("home-team-won", 0);
    }
}
