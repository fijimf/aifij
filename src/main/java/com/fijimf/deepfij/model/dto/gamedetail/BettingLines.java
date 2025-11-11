package com.fijimf.deepfij.model.dto.gamedetail;

public record BettingLines(
        Double pointSpread,
        Double overUnder,
        Integer homeMoneyLine,
        Integer awayMoneyLine
) {
}
