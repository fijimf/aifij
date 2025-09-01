package com.fijimf.deepfij.model.dto;

import java.time.LocalDate;
import java.util.List;

public record GamesByDateDTO(int season, LocalDate date, List<GameDTO> games) {
    
    public GamesByDateDTO {
        if (season <= 0) {
            throw new IllegalArgumentException("Season must be positive");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (games == null) {
            throw new IllegalArgumentException("Games list cannot be null");
        }
    }
}