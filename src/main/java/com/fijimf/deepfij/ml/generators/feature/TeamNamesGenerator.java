package com.fijimf.deepfij.ml.generators.feature;

import com.fijimf.deepfij.ml.FeatureGenerator;
import com.fijimf.deepfij.model.schedule.Game;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TeamNamesGenerator implements FeatureGenerator {

    @Override
    public String getKey() {
        return "team_names";
    }

    @Override
    public List<String> getModelsSupported() {
        return List.of("basic-margin", "logistic-regression");
    }

    @Override
    public Map<String, Object> generateFeature(Game game) {
        return Map.of("home_team", game.getHomeTeam().getAbbreviation(),
                "away_team", game.getAwayTeam().getAbbreviation());
    }
}
