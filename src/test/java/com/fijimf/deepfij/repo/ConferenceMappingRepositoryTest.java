package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.DeepFijApplication;
import com.fijimf.deepfij.model.schedule.Conference;
import com.fijimf.deepfij.model.schedule.ConferenceMapping;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import jakarta.validation.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = DeepFijApplication.class)
public class ConferenceMappingRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = SharedPostgreSQLContainer.getInstance();

    @BeforeAll
    static void setup() {
        // Ensure the container is started before setting system properties
        postgreSQLContainer.start(); // Explicitly ensure it starts
        System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
        System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
    }

    @Autowired
    private ConferenceMappingRepository conferenceMappingRepository;

    @Autowired
    private ConferenceRepository conferenceRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    private Conference conference;
    private Team team;
    private Season season;

    @BeforeEach
    void setupTestData() {
        // Clear any existing data
        conferenceMappingRepository.deleteAll();
        
        // Create and save a conference
        conference = createDummyConference("Big Ten", "B10", "big-ten");
        conference = conferenceRepository.save(conference);

        // Create and save a team
        team = createDummyTeam("Michigan", "MICH");
        team = teamRepository.save(team);

        // Create and save a season
        season = createDummySeason(2024, "2024-2025");
        season = seasonRepository.save(season);
    }

    @Test
    public void testInsertConferenceMapping() {
        // Create and save a conference mapping
        ConferenceMapping mapping = createDummyConferenceMapping(conference, team, season);
        mapping = conferenceMappingRepository.save(mapping);

        // Verify it was saved correctly
        assertThat(mapping.getId()).isNotNull();
        assertThat(conferenceMappingRepository.findById(mapping.getId())).isPresent();
        assertThat(conferenceMappingRepository.findAll()).hasSize(1);
    }

    @Test
    public void testFindBySeason() {
        // Create and save multiple conference mappings for the same season
        ConferenceMapping mapping1 = createDummyConferenceMapping(conference, team, season);
        conferenceMappingRepository.save(mapping1);

        // Create another team
        Team team2 = createDummyTeam("Ohio State", "OSU");
        team2 = teamRepository.save(team2);

        ConferenceMapping mapping2 = createDummyConferenceMapping(conference, team2, season);
        conferenceMappingRepository.save(mapping2);

        // Create another season
        Season season2 = createDummySeason(2023, "2023-2024");
        season2 = seasonRepository.save(season2);

        // Create a mapping for the second season
        ConferenceMapping mapping3 = createDummyConferenceMapping(conference, team, season2);
        conferenceMappingRepository.save(mapping3);

        // Test findBySeason
        List<ConferenceMapping> results = conferenceMappingRepository.findBySeason(season);
        
        assertThat(results).hasSize(2);
        assertThat(results).extracting("season.id").containsOnly(season.getId());
    }

    @Test
    public void testDeleteBySeason() {
        // Create and save multiple conference mappings for the same season
        ConferenceMapping mapping1 = createDummyConferenceMapping(conference, team, season);
        conferenceMappingRepository.save(mapping1);

        // Create another team
        Team team2 = createDummyTeam("Ohio State", "OSU");
        team2 = teamRepository.save(team2);

        ConferenceMapping mapping2 = createDummyConferenceMapping(conference, team2, season);
        conferenceMappingRepository.save(mapping2);

        // Create another season
        Season season2 = createDummySeason(2023, "2023-2024");
        season2 = seasonRepository.save(season2);

        // Create a mapping for the second season
        ConferenceMapping mapping3 = createDummyConferenceMapping(conference, team, season2);
        conferenceMappingRepository.save(mapping3);

        // Verify we have 3 mappings
        assertThat(conferenceMappingRepository.findAll()).hasSize(3);

        // Delete by the first season
        long deletedCount = conferenceMappingRepository.deleteBySeason(season);

        // Verify the deletion count and remaining mappings
        assertThat(deletedCount).isEqualTo(2);
        List<ConferenceMapping> remaining = conferenceMappingRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.getFirst().getSeason().getId()).isEqualTo(season2.getId());
    }

    @Test
    public void testValidationConstraints() {
        // Test with null season
        ConferenceMapping mapping1 = createDummyConferenceMapping(conference, team, null);
        assertThatThrownBy(() -> {
            conferenceMappingRepository.save(mapping1);
            conferenceMappingRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with null team
        ConferenceMapping mapping2 = createDummyConferenceMapping(conference, null, season);
        assertThatThrownBy(() -> {
            conferenceMappingRepository.save(mapping2);
            conferenceMappingRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with null conference
        ConferenceMapping mapping3 = createDummyConferenceMapping(null, team, season);
        assertThatThrownBy(() -> {
            conferenceMappingRepository.save(mapping3);
            conferenceMappingRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    private @NotNull Conference createDummyConference(String name, String shortName, String espnId) {
        Conference conference = new Conference();
        conference.setName(name);
        conference.setShortName(shortName);
        conference.setLogoUrl("https://example.com/" + (espnId != null ? espnId : "unknown") + ".png");
        conference.setEspnId(espnId);
        return conference;
    }

    private @NotNull Team createDummyTeam(String name, String abbr) {
        Team team = new Team();
        team.setName(name);
        team.setNickname(name + " Nickname");
        team.setLogoUrl("https://example.com/" + abbr.toLowerCase() + ".png");
        team.setPrimaryColor("#000000");
        team.setSecondaryColor("#FFFFFF");
        team.setSlug(name.toLowerCase().replace(" ", "-"));
        team.setEspnId(abbr.toLowerCase());
        team.setAbbreviation(abbr);
        team.setLongName(name + " University");
        return team;
    }

    private @NotNull Season createDummySeason(int year, String name) {
        Season season = new Season();
        season.setYear(year);
        season.setName(name);
        season.setStartDate(LocalDate.of(year, 11, 1));
        season.setEndDate(LocalDate.of(year + 1, 4, 30));
        return season;
    }

    private @NotNull ConferenceMapping createDummyConferenceMapping(Conference conference, Team team, Season season) {
        ConferenceMapping mapping = new ConferenceMapping();
        mapping.setConference(conference);
        mapping.setTeam(team);
        mapping.setSeason(season);
        return mapping;
    }
}