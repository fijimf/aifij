package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.DeepFijApplication;
import com.fijimf.deepfij.model.schedule.Team;
import jakarta.validation.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = DeepFijApplication.class)
public class TeamRepositoryTest {

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
    private TeamRepository teamRepository;


    @Test
    public void testInsertTeam_HappyPath() {
        // Happy Path: Insert a valid user
        Team team = createDummyTeam();
        team = teamRepository.save(team);
        assertThat(team.getId()).isGreaterThan(0L);
        assertThat(teamRepository.findById(team.getId())).isPresent();
        assertThat(teamRepository.findAll()).hasSize(1);
    }

    @Test
    public void testBadInserts_Validation() {
        Team t1 = createDummyTeam();
        t1.setName("");
        assertThatThrownBy(() -> {
            teamRepository.save(t1);
        }).isInstanceOf(ConstraintViolationException.class);

        Team t2 = createDummyTeam();
        t2.setName(null);
        assertThatThrownBy(() -> {
            teamRepository.save(t2);
        }).isInstanceOf(ConstraintViolationException.class);

        Team t3 = createDummyTeam();
        t3.setAbbreviation("");
        assertThatThrownBy(() -> {
            teamRepository.save(t3);
        }).isInstanceOf(ConstraintViolationException.class);

        Team t4 = createDummyTeam();
        t4.setAbbreviation("");
        assertThatThrownBy(() -> {
            teamRepository.save(t4);
        }).isInstanceOf(ConstraintViolationException.class);

        Team t5 = createDummyTeam();
        t5.setEspnId("");
        assertThatThrownBy(() -> {
            teamRepository.save(t5);
        }).isInstanceOf(ConstraintViolationException.class);

        Team t6 = createDummyTeam();
        t6.setSlug(null);
        assertThatThrownBy(() -> {
            teamRepository.save(t6);
        }).isInstanceOf(ConstraintViolationException.class);

    }

    @Test
    public void testBadInserts_DuplicateName() {
        Team team1 = createDummyTeam();
        team1 = teamRepository.save(team1);

        assertThatThrownBy(() -> {
            Team team2 = createDummyTeam();
            team2 = teamRepository.save(team2);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testBadUpdate_Duplicate() {
        Team team1 = createDummyTeam(1);
        team1 = teamRepository.save(team1);
        String espnId = team1.getEspnId();

        assertThatThrownBy(() -> {
            Team team2 = createDummyTeam(2);
            team2 = teamRepository.save(team2);
            team2.setEspnId(espnId);
            teamRepository.save(team2);
            teamRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    private static @NotNull Team createDummyTeam() {
        return createDummyTeam(0);
    }

    private static @NotNull Team createDummyTeam(int version) {
        Team team = new Team();
        team.setName("testteam" + version);
        team.setNickname("testteam" + version);
        team.setLogoUrl("https://example.com/testteam.png");
        team.setPrimaryColor("#000000");
        team.setSecondaryColor("#000000");
        team.setSlug("testteam" + version);
        team.setEspnId(Integer.toString(33 + version));
        team.setAbbreviation("TT" + version);
        team.setLongName("Test Team " + version);
        return team;
    }

}