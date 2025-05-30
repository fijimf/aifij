package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.DeepFijApplication;
import com.fijimf.deepfij.model.schedule.Game;
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
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = DeepFijApplication.class)
public class GameRepositoryTest {

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
    private GameRepository gameRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    private Team homeTeam;
    private Team awayTeam;
    private Season season;
    private LocalDate gameDate;

    @BeforeEach
    void setupTestData() {
        // Create and save teams
        homeTeam = createDummyTeam("Home Team", "HOME");
        homeTeam = teamRepository.save(homeTeam);

        awayTeam = createDummyTeam("Away Team", "AWAY");
        awayTeam = teamRepository.save(awayTeam);

        // Create and save a season
        season = createDummySeason(2024, "2024-2025");
        season = seasonRepository.save(season);

        // Set a game date
        gameDate = LocalDate.of(2024, 12, 15);

        // Clear any existing games
        gameRepository.deleteAll();
    }

    @Test
    public void testInsertGame() {
        // Create and save a game
        Game game = createDummyGame(homeTeam, awayTeam, season, gameDate);
        game = gameRepository.save(game);

        // Verify it was saved correctly
        assertThat(game.getId()).isNotNull();
        assertThat(gameRepository.findById(game.getId())).isPresent();
        assertThat(gameRepository.findAll()).hasSize(1);
    }

    @Test
    public void testFindBySeasonOrderByDateAsc() {
        // Create and save multiple games with different dates
        Game game1 = createDummyGame(homeTeam, awayTeam, season, gameDate);
        gameRepository.save(game1);

        Game game2 = createDummyGame(awayTeam, homeTeam, season, gameDate.plusDays(1));
        gameRepository.save(game2);

        Game game3 = createDummyGame(homeTeam, awayTeam, season, gameDate.minusDays(1));
        gameRepository.save(game3);

        // Test findBySeasonOrderByDateAsc
        List<Game> results = gameRepository.findBySeasonOrderByDateAsc(season);
        
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getDate()).isEqualTo(gameDate.minusDays(1));
        assertThat(results.get(1).getDate()).isEqualTo(gameDate);
        assertThat(results.get(2).getDate()).isEqualTo(gameDate.plusDays(1));
    }

    @Test
    public void testFindBySeasonAndIndexDate() {
        // Create and save games with different index dates
        Game game1 = createDummyGame(homeTeam, awayTeam, season, gameDate);
        game1.setIndexDate(gameDate);
        gameRepository.save(game1);

        Game game2 = createDummyGame(awayTeam, homeTeam, season, gameDate.plusDays(1));
        game2.setIndexDate(gameDate.plusDays(1));
        gameRepository.save(game2);

        // Test findBySeasonAndIndexDate
        List<Game> results = gameRepository.findBySeasonAndIndexDate(season, gameDate);
        
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getIndexDate()).isEqualTo(gameDate);
    }

    @Test
    public void testDeleteBySeason() {
        // Create and save games for the season
        Game game1 = createDummyGame(homeTeam, awayTeam, season, gameDate);
        gameRepository.save(game1);

        Game game2 = createDummyGame(awayTeam, homeTeam, season, gameDate.plusDays(1));
        gameRepository.save(game2);

        // Create another season
        Season season2 = createDummySeason(2023, "2023-2024");
        season2 = seasonRepository.save(season2);

        // Create a game for the second season
        Game game3 = createDummyGame(homeTeam, awayTeam, season2, gameDate);
        gameRepository.save(game3);

        // Verify we have 3 games
        assertThat(gameRepository.findAll()).hasSize(3);

        // Delete by the first season
        long deletedCount = gameRepository.deleteBySeason(season);

        // Verify the deletion count and remaining games
        assertThat(deletedCount).isEqualTo(2);
        List<Game> remaining = gameRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.getFirst().getSeason().getId()).isEqualTo(season2.getId());
    }

    @Test
    public void testFindTournamentGamesBySeason() {
        // Create and save regular games
        Game regularGame1 = createDummyGame(homeTeam, awayTeam, season, gameDate);
        gameRepository.save(regularGame1);

        Game regularGame2 = createDummyGame(awayTeam, homeTeam, season, gameDate.plusDays(1));
        gameRepository.save(regularGame2);

        // Create and save tournament games (with seeds)
        Game tournamentGame1 = createDummyGame(homeTeam, awayTeam, season, gameDate.plusDays(10));
        tournamentGame1.setHomeTeamSeed(1);
        tournamentGame1.setAwayTeamSeed(8);
        gameRepository.save(tournamentGame1);

        Game tournamentGame2 = createDummyGame(awayTeam, homeTeam, season, gameDate.plusDays(11));
        tournamentGame2.setHomeTeamSeed(4);
        tournamentGame2.setAwayTeamSeed(5);
        gameRepository.save(tournamentGame2);

        // Test findTournamentGamesBySeason
        List<Game> results = gameRepository.findTournamentGamesBySeason(season);
        
        assertThat(results).hasSize(2);
        // The query orders by date DESC, so the latest game should be first
        assertThat(results.get(0).getDate()).isEqualTo(gameDate.plusDays(11));
        assertThat(results.get(1).getDate()).isEqualTo(gameDate.plusDays(10));
        
        // Verify all results have seeds
        assertThat(results).allMatch(g -> g.getHomeTeamSeed() != null && g.getAwayTeamSeed() != null);
    }

    @Test
    public void testValidationConstraints() {
        // Test with null season
        Game game1 = createDummyGame(homeTeam, awayTeam, null, gameDate);
        assertThatThrownBy(() -> {
            gameRepository.save(game1);
            gameRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with null home team
        Game game2 = createDummyGame(null, awayTeam, season, gameDate);
        assertThatThrownBy(() -> {
            gameRepository.save(game2);
            gameRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with null away team
        Game game3 = createDummyGame(homeTeam, null, season, gameDate);
        assertThatThrownBy(() -> {
            gameRepository.save(game3);
            gameRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with null date
        Game game4 = createDummyGame(homeTeam, awayTeam, season, null);
        assertThatThrownBy(() -> {
            gameRepository.save(game4);
            gameRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);
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

    private @NotNull Game createDummyGame(Team homeTeam, Team awayTeam, Season season, LocalDate date) {
        Game game = new Game();
        game.setHomeTeam(homeTeam);
        game.setAwayTeam(awayTeam);
        game.setSeason(season);
        game.setDate(date);
        game.setTime(LocalTime.of(19, 0)); // 7:00 PM
        game.setIndexDate(date);
        game.setEspnId("espn-" + System.currentTimeMillis());
        game.setStatus("scheduled");
        game.setLocation("Test Arena");
        game.setNeutralSite(false);
        return game;
    }
}