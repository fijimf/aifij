package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RawTeamJsonParseTest {

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

            assertThat(wrapper.rawTeam()).isNotNull();
            RawTeam rawTeam = wrapper.rawTeam();
            //    System.out.println(team);
            assertThat(rawTeam).isNotNull();
            assertThat(rawTeam.uid()).isNotNull();
            assertThat(rawTeam.name()).isNotNull();
            assertThat(rawTeam.abbreviation()).isNotNull();
            assertThat(rawTeam.displayName()).isNotNull();
            // assertThat(team.alternateColor()).isNotNull(); Austin Peay
            assertThat(rawTeam.logos()).isNotNull();
            assertThat(rawTeam.logos()).hasSizeGreaterThanOrEqualTo(1);
            assertThat(rawTeam.nickname()).isNotNull();
            //     assertThat(team.primaryColor()).isNotNull(); LeMoyne
            assertThat(rawTeam.shortDisplayName()).isNotNull();
            assertThat(rawTeam.slug()).isNotNull();
        });

    }

    @Test
    public void testParseSingleTeamJson() throws IOException {
        String villanova = new String(getClass().getClassLoader()
                .getResourceAsStream("json/team1.json")
                .readAllBytes());

        ObjectMapper mapper = new ObjectMapper();
        TeamWrapper wrapper = mapper.readValue(villanova, TeamWrapper.class);
        RawTeam rawTeam = wrapper.rawTeam();
        assertThat(rawTeam).isNotNull();
        assertThat(rawTeam.id()).isNotNull();
        assertThat(rawTeam.id()).isEqualTo("222");
        assertThat(rawTeam.name()).isNotNull();
        assertThat(rawTeam.name()).isEqualTo("Wildcats");
        assertThat(rawTeam.uid()).isNotNull();
        assertThat(rawTeam.abbreviation()).isNotNull();
        assertThat(rawTeam.displayName()).isNotNull();
        // assertThat(team.alternateColor()).isNotNull(); Austin Peay
        assertThat(rawTeam.logos()).isNotNull();
        assertThat(rawTeam.logos()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(rawTeam.nickname()).isNotNull();
        //     assertThat(team.primaryColor()).isNotNull(); LeMoyne
        assertThat(rawTeam.shortDisplayName()).isNotNull();
        assertThat(rawTeam.slug()).isNotNull();
    }
}
