package com.fijimf.deepfij.model.scraping.standings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class StandingsJsonParseTest {
    @Test
    public void testParse2014Json() throws IOException {
        String json = new String(getClass().getClassLoader()
                .getResourceAsStream("json/standings2014.json")
                .readAllBytes());

        ObjectMapper mapper = new ObjectMapper();
        StandingsResponse response = mapper.readValue(json, StandingsResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.children()).isNotNull();
        assertThat(response.children()).hasSize(33);
        response.children().forEach(c -> conferenceAssertions(c, false));

    }


    @Test
    public void testParse2025Json() throws IOException {
        String json = new String(getClass().getClassLoader()
                .getResourceAsStream("json/standings2025.json")
                .readAllBytes());

        ObjectMapper mapper = new ObjectMapper();
        StandingsResponse response = mapper.readValue(json, StandingsResponse.class);

        assertThat(response).isNotNull();
        assertThat(response).isNotNull();
        assertThat(response.children()).isNotNull();
        assertThat(response.children()).hasSize(31);
        response.children().forEach(c -> conferenceAssertions(c, false));

    }

    @Test
    public void testParse2025AJson() throws IOException {
        String json = new String(getClass().getClassLoader()
                .getResourceAsStream("json/standings2025a.json")
                .readAllBytes());

        ObjectMapper mapper = new ObjectMapper();
        StandingsResponse response = mapper.readValue(json, StandingsResponse.class);

        assertThat(response).isNotNull();
        assertThat(response).isNotNull();
        assertThat(response.children()).isNotNull();
        assertThat(response.children()).hasSize(32);
        assertThat(response.children()).filteredOn(ConferenceStanding::isConference).hasSize(31);
        response.children().forEach(c -> conferenceAssertions(c, false));


    }

    @Test
    public void testParse2026EmptyJson() throws IOException {
        String json = new String(getClass().getClassLoader()
                .getResourceAsStream("json/emptyStandings2026.json")
                .readAllBytes());

        ObjectMapper mapper = new ObjectMapper();
        StandingsResponse response = mapper.readValue(json, StandingsResponse.class);

        assertThat(response).isNotNull();
        assertThat(response).isNotNull();
        assertThat(response.children()).isNotNull();
        assertThat(response.children()).hasSize(32);
        assertThat(response.children()).filteredOn(ConferenceStanding::isConference).hasSize(31);
        response.children().forEach(c -> conferenceAssertions(c, true));


    }

    private static void conferenceAssertions(ConferenceStanding child, boolean isEmpty) {
        //System.err.println(child.name());
        assertThat(child).isNotNull();
        assertThat(child.id()).isNotNull();
        assertThat(child.uid()).isNotNull();
        assertThat(child.shortName()).isNotNull();
        assertThat(child.name()).isNotNull();
        assertThat(child.abbreviation()).isNotNull();
        assertThat(child.consolidatedStandings()).isNotNull();
        if (isEmpty) {
            assertThat(child.consolidatedStandings()).isEmpty();
        } else {
            assertThat(child.consolidatedStandings()).hasSizeGreaterThanOrEqualTo(1);
            child.consolidatedStandings().forEach(standings -> {
                assertThat(standings.rawTeam()).isNotNull();
                assertThat(standings.rawTeam().id()).isNotNull();
                assertThat(standings.rawTeam().name()).isNotNull();
                assertThat(standings.rawTeam().shortDisplayName()).isNotNull();
                assertThat(standings.rawTeam().displayName()).isNotNull();
            });
        }
    }
}
