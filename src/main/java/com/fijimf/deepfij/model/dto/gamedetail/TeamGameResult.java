package com.fijimf.deepfij.model.dto.gamedetail;

import java.time.LocalDate;

public record TeamGameResult(
        LocalDate date,
        Long opponentId,
        String opponentName,
        String opponentAbbreviation,
        int teamScore,
        int opponentScore,
        boolean isWin
) {
}
