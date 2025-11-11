package com.fijimf.deepfij.model.dto.gamedetail;

import java.util.List;

public record TeamDetail(
        Long id,
        String name,
        String abbreviation,
        String logoUrl,
        ConferenceInfo conference,
        Integer score,
        WinLossRecord overallRecord,
        WinLossRecord conferenceRecord,
        WinLossRecord homeRecord,
        WinLossRecord awayRecord,
        WinLossRecord neutralRecord,
        WinLossRecord lastFiveRecord,
        Streak currentStreak,
        TeamStatistics statistics,
        List<TeamGameResult> seasonGames
) {
}
