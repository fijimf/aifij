package com.fijimf.deepfij.ml.generators.models;

import com.fijimf.deepfij.ml.ModelImpl;
import com.fijimf.deepfij.model.schedule.Game;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MarginNaiveLinearRegression implements ModelImpl {
    @Override
    public String name() {
        return "naive-linear-regression";
    }

    @Override
    public String description() {
        return "Naive linear regression of scores using a one-hot encoding of the home and away teams";
    }

    @Override
    public String type() {
        return "SingleSeason";
    }

    @Override
    public List<String> featureNames() {
        return List.of("homeTeam", "awayTeam");
    }

    @Override
    public List<String> labelNames() {
        return List.of("margin");
    }

    @Override
    public Map<String, Object> features(Game games) {
        return Map.of("homeTeam", games.getHomeTeam().getAbbreviation(),
                "awayTeam", games.getAwayTeam().getAbbreviation());
    }

    @Override
    public Map<String, Object> labels(Game games) {
        if (games.getHomeScore() == null || games.getAwayScore() == null) {
            throw new IllegalArgumentException("Home and away scores must be set");
        }
        return Map.of("margin", games.getHomeScore() - games.getAwayScore());
    }
}
