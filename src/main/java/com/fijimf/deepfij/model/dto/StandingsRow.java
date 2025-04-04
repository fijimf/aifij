package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.schedule.Record;
import com.fijimf.deepfij.model.schedule.Team;

import java.util.Comparator;

public record StandingsRow(Team team, com.fijimf.deepfij.model.schedule.Record conferenceRecord,
                           com.fijimf.deepfij.model.schedule.Record overallRecord) implements Comparable<StandingsRow> {
    @Override
    public int compareTo(StandingsRow o) {

        return Comparator
                .comparing(StandingsRow::conferenceRecord, com.fijimf.deepfij.model.schedule.Record.NATURAL_ORDER)
                .thenComparing(StandingsRow::overallRecord, Record.NATURAL_ORDER)
                .thenComparing(row -> row.team().getName(), String.CASE_INSENSITIVE_ORDER)
                .compare(this, o);
    }
}
