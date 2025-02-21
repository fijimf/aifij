package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fijimf.deepfij.model.scraping.conference.ConferenceResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TeamJsonParseTest {

    @Test
    public void testParseJson() throws IOException {
        String json = new String(getClass().getClassLoader()
                .getResourceAsStream("json/teams.json")
                .readAllBytes());

        ObjectMapper mapper = new ObjectMapper();
        SportsResponse response = mapper.readValue(json, SportsResponse.class);
        assertThat(response).isNotNull();
        List<Sport> sports = response.sports();
        assertThat(sports).isNotNull();
        assertThat(sports).hasSize(1);
        List<League> leagues = sports.getFirst().leagues();
        assertThat(leagues).isNotNull();
        assertThat(leagues).hasSize(1);
        List<TeamWrapper> teams = leagues.getFirst().teams();
        assertThat(teams).isNotNull();
        assertThat(teams).hasSize(361);
        teams.forEach(wrapper -> {

            assertThat(wrapper.team()).isNotNull();
            Team team = wrapper.team();
        //    System.out.println(team);
            assertThat(team).isNotNull();
            assertThat(team.uid()).isNotNull();
            assertThat(team.name()).isNotNull();
            assertThat(team.abbreviation()).isNotNull();
            assertThat(team.displayName()).isNotNull();
           // assertThat(team.alternateColor()).isNotNull(); Austin Peay
            assertThat(team.logos()).isNotNull();
            assertThat(team.logos()).hasSizeGreaterThanOrEqualTo(1);
            assertThat(team.nickname()).isNotNull();
       //     assertThat(team.primaryColor()).isNotNull(); LeMoyne
            assertThat(team.shortDisplayName()).isNotNull();
            assertThat(team.slug()).isNotNull();
        });

    }
}
