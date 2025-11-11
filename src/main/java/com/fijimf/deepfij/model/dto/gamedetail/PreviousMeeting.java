package com.fijimf.deepfij.model.dto.gamedetail;

import java.time.LocalDate;

public record PreviousMeeting(
        LocalDate date,
        String homeTeamName,
        String homeTeamAbbreviation,
        int homeTeamScore,
        String awayTeamName,
        String awayTeamAbbreviation,
        int awayTeamScore
) {
}
