package com.fijimf.deepfij.ml;

import com.fijimf.deepfij.model.schedule.Game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface FeatureGenerator {
    String getKey();

    List<String> getModelsSupported();


    Map<String, Object> generateFeature(Game game);
}
