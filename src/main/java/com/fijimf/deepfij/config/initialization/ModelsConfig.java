package com.fijimf.deepfij.config.initialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import java.util.List;

public record ModelsConfig(
        @JsonProperty("modelsToTrain") 
        @Valid
        List<ModelToTrainConfig> modelsToTrain
) {
    public ModelsConfig() {
        this(List.of());
    }
}