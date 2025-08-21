package com.fijimf.deepfij.ml;

import com.fijimf.deepfij.model.schedule.Game;

import java.util.List;
import java.util.Map;

interface ModelImpl {
    String name();
    String description();
    String type();
    Map<String, Object> features(List<Game> games);
    Map<String, Object> labels(List<Game> games);
 }
