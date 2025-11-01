package com.fijimf.deepfij.model.scraping.scoreboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class ScoreboardJsonParseTest {

    @Test
    public void testParseJson1() throws IOException {
        String json = new String(Objects.requireNonNull(getClass().getClassLoader()
                        .getResourceAsStream("json/scoreboard20250226.json")).readAllBytes());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        ScoreboardResponse response = mapper.readValue(json, ScoreboardResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.sports()).isNotNull();
        assertThat(response.sports()).hasSize(1);
        assertThat(response.sports().getFirst().leagues()).isNotNull();
        assertThat(response.sports().getFirst().leagues()).hasSize(1);
        assertThat(response.sports().getFirst().leagues().getFirst().events()).isNotNull();
        assertThat(response.sports().getFirst().leagues().getFirst().events()).hasSize(53);
        assertThat(response.events()).hasSize(53);
    }

    @Test
    public void testParseJson2() throws IOException {
        String json = new String(Objects.requireNonNull(getClass().getClassLoader()
                        .getResourceAsStream("json/scoreboard20250119.json"))
                .readAllBytes());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        ScoreboardResponse response = mapper.readValue(json, ScoreboardResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.sports()).isNotNull();
        assertThat(response.sports()).hasSize(1);
        assertThat(response.sports().getFirst().leagues()).isNotNull();
        assertThat(response.sports().getFirst().leagues()).hasSize(1);
        assertThat(response.sports().getFirst().leagues().getFirst().events()).isNotNull();
        assertThat(response.sports().getFirst().leagues().getFirst().events()).hasSize(11);
        assertThat(response.events()).hasSize(11);
    }

    @Test
    public void testParseJson3() throws IOException {
        String json = new String(Objects.requireNonNull(getClass().getClassLoader()
                        .getResourceAsStream("json/scoreboard20251103.json"))
                .readAllBytes());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        ScoreboardResponse response = mapper.readValue(json, ScoreboardResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.sports()).isNotNull();
        assertThat(response.sports()).hasSize(1);
        assertThat(response.sports().getFirst().leagues()).isNotNull();
        assertThat(response.sports().getFirst().leagues()).hasSize(1);
        assertThat(response.sports().getFirst().leagues().getFirst().events()).isNotNull();
        assertThat(response.sports().getFirst().leagues().getFirst().events()).hasSize(169);
        assertThat(response.events()).hasSize(169);
        response.events().forEach(event -> {
            if (event.uid().equals("s:40~l:41~e:401819834~c:401819834")) {
                assertThat(event.odds().overUnder()).isEqualTo(153.5);
                assertThat(event.odds().spread()).isEqualTo(-8.5);
                assertThat(event.odds().away().moneyLine()).isEqualTo(270);
                assertThat(event.odds().home().moneyLine()).isEqualTo(-340);
            }
        });
    }
}
