package com.fijimf.deepfij.ml;

import com.fijimf.deepfij.model.schedule.Game;

import java.util.List;
import java.util.Map;

public interface TargetGenerator {
    String getKey();

    List<String> getModelsSupported();

    Map<String, Object> generateTarget(Game game);
}
