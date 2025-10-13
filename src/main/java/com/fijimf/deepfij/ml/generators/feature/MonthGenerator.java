package com.fijimf.deepfij.ml.generators.feature;

import com.fijimf.deepfij.ml.FeatureGenerator;
import com.fijimf.deepfij.model.schedule.Game;

import java.util.List;
import java.util.Map;

public class MonthGenerator implements FeatureGenerator {
    @Override
    public String getKey() {
        return "month";
    }


    public int generateFeature(Game game) {
        return game.getDate().getMonthValue();
    }
}
