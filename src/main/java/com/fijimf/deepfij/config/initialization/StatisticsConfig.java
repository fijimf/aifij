package com.fijimf.deepfij.config.initialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record StatisticsConfig(
        @JsonProperty("statisticsToLoad") 
        @Valid
        List<StatisticToLoadConfig> statisticsToLoad
) {
    public StatisticsConfig() {
        this(List.of());
    }
}