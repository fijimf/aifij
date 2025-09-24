package com.fijimf.deepfij.config.initialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SeasonConfig(
        @JsonProperty("year") 
        @NotNull(message = "year cannot be null")
        @Min(value = 2000, message = "year must be at least 2000")
        @Max(value = 2100, message = "year must be at most 2100")
        Integer year,
        
        @JsonProperty("loadGames") 
        @Pattern(regexp = "always|check|never", message = "loadGames must be 'always', 'check', or 'never'")
        String loadGames,
        
        @JsonProperty("isCurrent") 
        Boolean isCurrent
) {
    public SeasonConfig() {
        this(null, "never", false);
    }
}