package com.fijimf.deepfij.model.scraping.scoreboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ScoreboardJsonParseTest {

    @Test
    public void testParseJson1() throws IOException {
        String json = new String(getClass().getClassLoader()
                .getResourceAsStream("json/scoreboard20250226.json")
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
        assertThat(response.sports().getFirst().leagues().getFirst().events()).hasSize(53);
assertThat(response.events()).hasSize(53);
    }

    @Test
    public void testParseJson2() throws IOException {
        String json = new String(getClass().getClassLoader()
                .getResourceAsStream("json/scoreboard20250119.json")
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
}
