package com.fijimf.deepfij.config.initialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record ScheduleConfig(
        @JsonProperty("loadTeams") 
        @Pattern(regexp = "always|check|never", message = "loadTeams must be 'always', 'check', or 'never'")
        String loadTeams,
        
        @JsonProperty("minTeams") 
        @Min(value = 1, message = "minTeams must be at least 1")
        Integer minTeams,
        
        @JsonProperty("loadConferences") 
        @Pattern(regexp = "always|check|never", message = "loadConferences must be 'always', 'check', or 'never'")
        String loadConferences,
        
        @JsonProperty("minConferences") 
        @Min(value = 1, message = "minConferences must be at least 1")
        Integer minConferences,
        
        @JsonProperty("seasons") 
        @NotEmpty(message = "seasons list cannot be empty")
        @Valid
        List<SeasonConfig> seasons
) {
    public ScheduleConfig() {
        this("never", 300, "never", 10, List.of());
    }
}