package com.fijimf.deepfij.model.dto.gamedetail;

public record PredictedValues(
        Double predictedSpread,
        Double predictedOverUnder,
        Double homeWinProbability,
        Double awayWinProbability,
        Integer predictedHomeMoneyLine,
        Integer predictedAwayMoneyLine
) {
}
