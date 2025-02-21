package com.fijimf.deepfij.model.scraping.conference;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ConferenceJsonParseTest {
    @Test
    public void testParseJson() throws IOException {
        String json = new String(getClass().getClassLoader()
                .getResourceAsStream("json/conferences.json")
                .readAllBytes());

        ObjectMapper mapper = new ObjectMapper();
        ConferenceResponse response = mapper.readValue(json, ConferenceResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.conferences()).isNotNull();
        assertThat(response.conferences()).hasSize(33);
        response.conferences().forEach(conference -> {
            System.out.println(conference);
            assertThat(conference).isNotNull();
            assertThat(conference.groupId()).isNotNull();
            if (conference.groupId().equals("50")) {
                assertThat(conference.name()).isEqualTo("NCAA Division I");
            } else if (conference.groupId().equals("0")) {
                assertThat(conference.shortName()).isEqualTo("Top 25");
            } else {

                assertThat(conference.uid()).isNotNull();
                assertThat(conference.name()).isNotNull();
                assertThat(conference.shortName()).isNotNull();
             //   assertThat(conference.logo()).isNotNull();  ASUN apparently is missing a logo
                assertThat(conference.parentGroupId()).isEqualTo("50");
            }
        });
    }
}

