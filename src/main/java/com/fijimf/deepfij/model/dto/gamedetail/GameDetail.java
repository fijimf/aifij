package com.fijimf.deepfij.model.dto.gamedetail;

public record GameDetail(
        GameInfo game,
        TeamDetail homeTeam,
        TeamDetail awayTeam
) {
}
