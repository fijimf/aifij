package com.fijimf.deepfij.ml.generators.target;

import com.fijimf.deepfij.ml.TargetGenerator;
import com.fijimf.deepfij.model.schedule.Game;

import java.util.List;
import java.util.Map;

public class ScoresGenerator implements TargetGenerator {
    @Override
    public String getKey() {
        return "scores";
    }

    @Override
    public List<String> getModelsSupported() {
        return List.of();
    }

    @Override
    public Map<String, Object> generateTarget(Game game) {
        return Map.of("homeScore", game.getHomeScore(), "awayScore", game.getAwayScore());
    }
}
