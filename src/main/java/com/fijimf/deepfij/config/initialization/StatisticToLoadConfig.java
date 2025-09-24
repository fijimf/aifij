package com.fijimf.deepfij.config.initialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record StatisticToLoadConfig(
        @JsonProperty("key") 
        @NotBlank(message = "statistic key cannot be blank")
        String key,
        
        @JsonProperty("seasons") 
        @NotEmpty(message = "seasons list cannot be empty")
        List<Integer> seasons
) {
    public StatisticToLoadConfig() {
        this("", List.of());
    }
}