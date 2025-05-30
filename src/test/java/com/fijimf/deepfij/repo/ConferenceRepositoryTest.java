package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.DeepFijApplication;
import com.fijimf.deepfij.model.schedule.Conference;
import jakarta.validation.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = DeepFijApplication.class)
public class ConferenceRepositoryTest {

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
    private ConferenceRepository conferenceRepository;

    @BeforeEach
    void setupTestData() {
        // Clear any existing conferences
        conferenceRepository.deleteAll();
    }

    @Test
    public void testInsertConference() {
        // Create and save a conference
        Conference conference = createDummyConference("Big Ten", "B10", "big-ten");
        conference = conferenceRepository.save(conference);

        // Verify it was saved correctly
        assertThat(conference.getId()).isNotNull();
        assertThat(conferenceRepository.findById(conference.getId())).isPresent();
        assertThat(conferenceRepository.findAll()).hasSize(1);
    }

    @Test
    public void testFindByEspnId() {
        // Create and save conferences with different ESPN IDs
        Conference conference1 = createDummyConference("Big Ten", "B10", "big-ten");
        conferenceRepository.save(conference1);

        Conference conference2 = createDummyConference("ACC", "ACC", "acc");
        conferenceRepository.save(conference2);

        // Test findByEspnId
        List<Conference> results = conferenceRepository.findByEspnId("big-ten");
        
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("Big Ten");
    }

    @Test
    public void testDeleteAll() {
        // Create and save multiple conferences
        Conference conference1 = createDummyConference("Big Ten", "B10", "big-ten");
        conferenceRepository.save(conference1);

        Conference conference2 = createDummyConference("ACC", "ACC", "acc");
        conferenceRepository.save(conference2);

        Conference conference3 = createDummyConference("SEC", "SEC", "sec");
        conferenceRepository.save(conference3);

        // Verify we have 3 conferences
        assertThat(conferenceRepository.findAll()).hasSize(3);

        // Delete all conferences
        int deletedCount = conferenceRepository.delete();

        // Verify the deletion count and remaining conferences
        assertThat(deletedCount).isEqualTo(3);
        assertThat(conferenceRepository.findAll()).isEmpty();
    }

    @Test
    public void testUniqueNameConstraint() {
        // Create and save a conference
        Conference conference1 = createDummyConference("Big Ten", "B10", "big-ten");
        conferenceRepository.save(conference1);

        // Try to create another with the same name
        Conference conference2 = createDummyConference("Big Ten", "B11", "big-ten-2");
        assertThatThrownBy(() -> {
            conferenceRepository.save(conference2);
            conferenceRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testUniqueShortNameConstraint() {
        // Create and save a conference
        Conference conference1 = createDummyConference("Big Ten", "B10", "big-ten");
        conferenceRepository.save(conference1);

        // Try to create another with the same short name
        Conference conference2 = createDummyConference("Big 10", "B10", "big-10");
        assertThatThrownBy(() -> {
            conferenceRepository.save(conference2);
            conferenceRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testValidationConstraints() {
        // Test with null name
        Conference conference1 = createDummyConference(null, "B10", "big-ten");
        assertThatThrownBy(() -> {
            conferenceRepository.save(conference1);
            conferenceRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with empty name
        Conference conference2 = createDummyConference("", "B10", "big-ten");
        assertThatThrownBy(() -> {
            conferenceRepository.save(conference2);
            conferenceRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with null short name
        Conference conference3 = createDummyConference("Big Ten", null, "big-ten");
        assertThatThrownBy(() -> {
            conferenceRepository.save(conference3);
            conferenceRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with empty short name
        Conference conference4 = createDummyConference("Big Ten", "", "big-ten");
        assertThatThrownBy(() -> {
            conferenceRepository.save(conference4);
            conferenceRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void testUpdateConference() {
        // Create and save a conference
        Conference conference = createDummyConference("Big Ten", "B10", "big-ten");
        conference = conferenceRepository.save(conference);
        Long id = conference.getId();

        // Update the conference
        conference.setLogoUrl("https://example.com/updated-logo.png");
        conferenceRepository.save(conference);

        // Verify the update
        Conference updated = conferenceRepository.findById(id).orElseThrow();
        assertThat(updated.getLogoUrl()).isEqualTo("https://example.com/updated-logo.png");
    }

    private @NotNull Conference createDummyConference(String name, String shortName, String espnId) {
        Conference conference = new Conference();
        conference.setName(name);
        conference.setShortName(shortName);
        conference.setLogoUrl("https://example.com/" + (espnId != null ? espnId : "unknown") + ".png");
        conference.setEspnId(espnId);
        return conference;
    }
}