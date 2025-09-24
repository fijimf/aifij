package com.fijimf.deepfij.config.initialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record ModelToTrainConfig(
        @JsonProperty("name") 
        @NotBlank(message = "model name cannot be blank")
        String name,
        
        @JsonProperty("parameters") 
        Map<String, Object> parameters
) {
    public ModelToTrainConfig() {
        this("", Map.of());
    }
}