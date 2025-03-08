package com.fijimf.deepfij.model.scraping.team;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RawTeamTest {

    @Test
    void fixColor() {
        assertThat(RawTeam.fixColor("ff00ab")).isEqualTo("#ff00ab");
        assertThat(RawTeam.fixColor(null)).isNull();
        assertThat(RawTeam.fixColor("blue")).isNull();
    }

    @Test
    void makeSlug() {
        assertThat(RawTeam.makeSlug("St. John's Red Storm")).isEqualTo("st-johns-red-storm");
        assertThat(RawTeam.makeSlug("Texas A&M-Corpus Christi Islanders")).isEqualTo("texas-am-corpus-christi-islanders");
        assertThat(RawTeam.makeSlug("Maryland Eastern Shore Hawks")).isEqualTo("maryland-eastern-shore-hawks");
        assertThat(RawTeam.makeSlug("Georgetown Hoyas")).isEqualTo("georgetown-hoyas");

    }
}