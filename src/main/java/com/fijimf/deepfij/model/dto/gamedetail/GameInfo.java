package com.fijimf.deepfij.model.dto.gamedetail;

import java.time.LocalDate;
import java.util.List;

public record GameInfo(
        Long id,
        LocalDate date,
        ConferenceInfo conference,
        HeadToHeadRecord headToHead,
        List<PreviousMeeting> lastFiveMeetings,
        BettingLines bettingLines,
        PredictedValues predictions
) {
}
