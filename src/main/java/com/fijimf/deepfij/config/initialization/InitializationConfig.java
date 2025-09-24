package com.fijimf.deepfij.config.initialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record InitializationConfig(
        @JsonProperty("schedule") @Valid ScheduleConfig schedule,
        @JsonProperty("statistics") @Valid StatisticsConfig statistics,
        @JsonProperty("models") @Valid ModelsConfig models
) {
    public InitializationConfig() {
        this(new ScheduleConfig(), new StatisticsConfig(), new ModelsConfig());
    }
}