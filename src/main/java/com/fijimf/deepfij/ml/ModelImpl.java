package com.fijimf.deepfij.ml;

import com.fijimf.deepfij.model.schedule.Game;

import java.util.List;
import java.util.Map;

public interface ModelImpl {
    String name();
    String description();
    String type();
    Map<String, Object> features(Game game);
    Map<String, Object> labels(Game games);
 }
