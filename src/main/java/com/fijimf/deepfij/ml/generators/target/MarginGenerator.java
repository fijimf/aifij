package com.fijimf.deepfij.ml.generators.target;

import com.fijimf.deepfij.ml.TargetGenerator;
import com.fijimf.deepfij.model.schedule.Game;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MarginGenerator implements TargetGenerator {


    @Override
    public String getKey() {
        return "margin";
    }

    @Override
    public List<String> getModelsSupported() {
        return List.of("basic-margin");
    }

    @Override
    public Map<String, Object> generateTarget(Game game) {
        return Map.of("margin", game.getHomeScore() - game.getAwayScore());
    }
}
