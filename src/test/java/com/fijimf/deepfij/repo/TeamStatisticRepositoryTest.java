package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.DeepFijApplication;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = DeepFijApplication.class)
public class TeamStatisticRepositoryTest {

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
    private TeamStatisticRepository teamStatisticRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private StatisticTypeRepository statisticTypeRepository;

    private Team team;
    private Season season;
    private StatisticType statisticType;
    private LocalDate statisticDate;

    @BeforeEach
    void setupTestData() {
        // Create and save a team
        team = createDummyTeam();
        team = teamRepository.save(team);

        // Create and save a season
        season = createDummySeason();
        season = seasonRepository.save(season);

        // Create and save a statistic type
        statisticType = createDummyStatisticType();
        statisticType = statisticTypeRepository.save(statisticType);

        // Set a statistic date
        statisticDate = LocalDate.now();

        // Clear any existing team statistics
        teamStatisticRepository.deleteAll();
    }

    @Test
    public void testInsertTeamStatistic() {
        // Create and save a team statistic
        TeamStatistic teamStatistic = createDummyTeamStatistic(team, season, statisticType, statisticDate);
        teamStatistic = teamStatisticRepository.save(teamStatistic);

        // Verify it was saved correctly
        assertThat(teamStatistic.getId()).isNotNull();
        assertThat(teamStatisticRepository.findById(teamStatistic.getId())).isPresent();
        assertThat(teamStatisticRepository.findAll()).hasSize(1);
    }

    @Test
    public void testFindBySeasonIdAndStatisticTypeId() {
        // Create and save multiple team statistics
        TeamStatistic teamStatistic1 = createDummyTeamStatistic(team, season, statisticType, statisticDate);
        teamStatisticRepository.save(teamStatistic1);

        // Create another team
        Team team2 = createDummyTeam();
        team2.setName("Team 2");
        team2.setNickname("Team 2");
        team2.setAbbreviation("TM2");
        team2.setSlug("team-2");
        team2.setLongName("Team Two");
        team2.setEspnId("2");
        team2 = teamRepository.save(team2);

        // Create another statistic for the same season and statistic type
        TeamStatistic teamStatistic2 = createDummyTeamStatistic(team2, season, statisticType, statisticDate);
        teamStatisticRepository.save(teamStatistic2);

        // Test findBySeasonIdAndStatisticTypeId
        List<TeamStatistic> results = teamStatisticRepository.findBySeasonIdAndStatisticTypeId(
                season.getId(), statisticType.getId());
        
        assertThat(results).hasSize(2);
        assertThat(results).extracting("team.id").contains(team.getId(), team2.getId());
    }

    @Test
    public void testFindBySeasonIdAndStatisticTypeIdAndStatisticDate() {
        // Create and save a team statistic
        TeamStatistic teamStatistic = createDummyTeamStatistic(team, season, statisticType, statisticDate);
        teamStatisticRepository.save(teamStatistic);

        // Create another statistic for a different date
        LocalDate differentDate = statisticDate.plusDays(1);
        TeamStatistic teamStatistic2 = createDummyTeamStatistic(team, season, statisticType, differentDate);
        teamStatisticRepository.save(teamStatistic2);

        // Test findBySeasonIdAndStatisticTypeIdAndStatisticDate
        List<TeamStatistic> results = teamStatisticRepository.findBySeasonIdAndStatisticTypeIdAndStatisticDate(
                season.getId(), statisticType.getId(), statisticDate);
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatisticDate()).isEqualTo(statisticDate);
    }

    @Test
    public void testDeleteBySeason() {
        // Create and save a team statistic
        TeamStatistic teamStatistic = createDummyTeamStatistic(team, season, statisticType, statisticDate);
        teamStatisticRepository.save(teamStatistic);

        // Create another season
        Season season2 = createDummySeason();
        season2.setYear(2023);
        season2.setName("2023-2024");
        season2 = seasonRepository.save(season2);

        // Create a statistic for the second season
        TeamStatistic teamStatistic2 = createDummyTeamStatistic(team, season2, statisticType, statisticDate);
        teamStatisticRepository.save(teamStatistic2);

        // Verify we have 2 statistics
        assertThat(teamStatisticRepository.findAll()).hasSize(2);

        // Delete by the first season
        teamStatisticRepository.deleteBySeason(season);

        // Verify only the second season's statistic remains
        List<TeamStatistic> remaining = teamStatisticRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getSeason().getId()).isEqualTo(season2.getId());
    }

    private @NotNull Team createDummyTeam() {
        Team team = new Team();
        team.setName("Test Team");
        team.setNickname("Testers");
        team.setLogoUrl("https://example.com/testteam.png");
        team.setPrimaryColor("#000000");
        team.setSecondaryColor("#FFFFFF");
        team.setSlug("test-team");
        team.setEspnId("123");
        team.setAbbreviation("TST");
        team.setLongName("Test Team University");
        return team;
    }

    private @NotNull Season createDummySeason() {
        Season season = new Season();
        season.setYear(2024);
        season.setName("2024-2025");
        season.setStartDate(LocalDate.of(2024, 11, 1));
        season.setEndDate(LocalDate.of(2025, 4, 30));
        return season;
    }

    private @NotNull StatisticType createDummyStatisticType() {
        StatisticType statisticType = new StatisticType();
        statisticType.setName("Points Per Game");
        statisticType.setCode("PPG");
        statisticType.setDescription("Average points scored per game");
        statisticType.setIsHigherBetter(true);
        statisticType.setDecimalPlaces(2);
        return statisticType;
    }

    private @NotNull TeamStatistic createDummyTeamStatistic(Team team, Season season, StatisticType statisticType, LocalDate date) {
        TeamStatistic teamStatistic = new TeamStatistic();
        teamStatistic.setTeam(team);
        teamStatistic.setSeason(season);
        teamStatistic.setStatisticType(statisticType);
        teamStatistic.setStatisticDate(date);
        teamStatistic.setNumericValue(new BigDecimal("75.5"));
        teamStatistic.setLastUpdatedAt(ZonedDateTime.now());
        return teamStatistic;
    }
}