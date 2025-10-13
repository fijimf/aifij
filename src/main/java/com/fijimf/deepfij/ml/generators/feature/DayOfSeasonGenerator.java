package com.fijimf.deepfij.ml.generators.feature;

import com.fijimf.deepfij.ml.FeatureGenerator;
import com.fijimf.deepfij.model.schedule.Game;

import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

public class DayOfSeasonGenerator implements FeatureGenerator {
    @Override
    public String getKey() {
        return "day_of_season";
    }


    public long generateFeature(Game game) {
        return DAYS.between(game.getSeason().getStartDate(), game.getDate());
    }
}
