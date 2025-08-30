package com.fijimf.deepfij.model.ml;

import com.fijimf.deepfij.model.schedule.Game;

import java.util.Map;

public record GamePrediction(Game game, Map<String, Object> predictions) {
}
