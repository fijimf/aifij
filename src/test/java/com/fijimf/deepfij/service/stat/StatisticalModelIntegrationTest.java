package com.fijimf.deepfij.service.stat;

import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repo.*;
import com.fijimf.deepfij.service.StatisticTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Integration test for StatisticalModel implementations.
 * Uses a small, manually verifiable test dataset:
 * - 4 teams (Alpha, Beta, Gamma, Delta)
 * - Simple game schedule with known results
 * - Expected statistics calculated manually
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        WonLostStatisticModel.class,
        PointsStatisticModel.class,
        DecayingWonLostStatisticModel.class,
        RpiStatisticModel.class,
        NormalizedPointsStatisticModel.class,
        StatisticTypeService.class
})
public class StatisticalModelIntegrationTest {

    @Container
    static SharedPostgreSQLContainer postgreSQLContainer = SharedPostgreSQLContainer.getInstance();

    @DynamicPropertySource
    static void configureTestDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private StatisticTypeRepository statisticTypeRepository;

    @Autowired
    private WonLostStatisticModel wonLostModel;

    @Autowired
    private PointsStatisticModel pointsModel;

    @Autowired
    private DecayingWonLostStatisticModel decayingWonLostModel;

    @Autowired
    private RpiStatisticModel rpiModel;

    @Autowired
    private NormalizedPointsStatisticModel normalizedPointsModel;

    @Autowired
    private StatisticTypeService statisticTypeService;

    private Season testSeason;
    private Team alpha;
    private Team beta;
    private Team gamma;
    private Team delta;

    /**
     * Test schedule (4 teams, 4 games total):
     *
     * Day 1 (2025-01-01):
     *   Alpha 80 vs Beta 70 (Alpha wins)
     *
     * Day 2 (2025-01-02):
     *   Gamma 90 vs Delta 85 (Gamma wins)
     *
     * Day 3 (2025-01-03):
     *   Alpha 75 vs Gamma 80 (Gamma wins)
     *
     * Day 4 (2025-01-04):
     *   Beta 88 vs Delta 82 (Beta wins)
     */
    @BeforeEach
    void setUp() {
        // Create season
        testSeason = new Season();
        testSeason.setYear(2025);
        testSeason.setName("2025 Test Season");
        testSeason.setStartDate(LocalDate.of(2025, 1, 1));
        testSeason.setEndDate(LocalDate.of(2025, 3, 31));
        testSeason = seasonRepository.save(testSeason);

        // Create teams
        alpha = createTeam("Alpha", "ALF", "alpha-university", "Alpha University", "1");
        beta = createTeam("Beta", "BET", "beta-university", "Beta University", "2");
        gamma = createTeam("Gamma", "GAM", "gamma-university", "Gamma University", "3");
        delta = createTeam("Delta", "DEL", "delta-university", "Delta University", "4");

        // Create games
        createGame("g1", LocalDate.of(2025, 1, 1), alpha, beta, 80, 70);
        createGame("g2", LocalDate.of(2025, 1, 2), gamma, delta, 90, 85);
        createGame("g3", LocalDate.of(2025, 1, 3), alpha, gamma, 75, 80);
        createGame("g4", LocalDate.of(2025, 1, 4), beta, delta, 88, 82);

        // Initialize statistic types for all models
        wonLostModel.refreshDBTypes();
        pointsModel.refreshDBTypes();
        decayingWonLostModel.refreshDBTypes();
        rpiModel.refreshDBTypes();
        normalizedPointsModel.refreshDBTypes();
    }

    //TODO test every date
    @Test
    void testWonLostStatistics() {
        // Generate statistics
        List<TeamStatistic> stats = wonLostModel.generate(testSeason);

        // Get final day statistics (2025-01-04)
        Map<String, TeamStatistic> finalStats = getStatsForDate(stats, LocalDate.of(2025, 1, 4));

        // Alpha: 1 win, 1 loss, 50% win rate
        assertStat(finalStats, alpha, "WINS", 1.0);
        assertStat(finalStats, alpha, "LOSSES", 1.0);
        assertStat(finalStats, alpha, "WIN_PCT", 0.5);

        // Beta: 1 win, 1 loss, 50% win rate
        assertStat(finalStats, beta, "WINS", 1.0);
        assertStat(finalStats, beta, "LOSSES", 1.0);
        assertStat(finalStats, beta, "WIN_PCT", 0.5);

        // Gamma: 2 wins, 0 losses, 100% win rate
        assertStat(finalStats, gamma, "WINS", 2.0);
        assertStat(finalStats, gamma, "LOSSES", 0.0);
        assertStat(finalStats, gamma, "WIN_PCT", 1.0);

        // Delta: 0 wins, 2 losses, 0% win rate
        assertStat(finalStats, delta, "WINS", 0.0);
        assertStat(finalStats, delta, "LOSSES", 2.0);
        assertStat(finalStats, delta, "WIN_PCT", 0.0);
    }

    @Test
    void testPointsStatistics() {
        // Generate statistics
        List<TeamStatistic> stats = pointsModel.generate(testSeason);

        // Get final day statistics
        Map<String, TeamStatistic> finalStats = getStatsForDate(stats, LocalDate.of(2025, 1, 4));

        // Alpha: scored 80, 75 (avg=77.5, margin avg=(80-70)+(75-80)/2=2.5)
        assertStat(finalStats, alpha, "POINTS_FOR_AVG", 77.5);
        assertStat(finalStats, alpha, "POINTS_AGAINST_AVG", 75.0);
        assertStat(finalStats, alpha, "MARGIN_AVG", 2.5);

        // Beta: scored 70, 88 (avg=79.0, margin avg=(70-80)+(88-82)/2=-2.0)
        assertStat(finalStats, beta, "POINTS_FOR_AVG", 79.0);
        assertStat(finalStats, beta, "POINTS_AGAINST_AVG", 81.0);
        assertStat(finalStats, beta, "MARGIN_AVG", -2.0);

        // Gamma: scored 90, 80 (avg=85.0, margin avg=(90-85)+(80-75)/2=5.0)
        assertStat(finalStats, gamma, "POINTS_FOR_AVG", 85.0);
        assertStat(finalStats, gamma, "POINTS_AGAINST_AVG", 80.0);
        assertStat(finalStats, gamma, "MARGIN_AVG", 5.0);

        // Delta: scored 85, 82 (avg=83.5, margin avg=(85-90)+(82-88)/2=-5.5)
        assertStat(finalStats, delta, "POINTS_FOR_AVG", 83.5);
        assertStat(finalStats, delta, "POINTS_AGAINST_AVG", 89.0);
        assertStat(finalStats, delta, "MARGIN_AVG", -5.5);
    }

    @Test
    void testDecayingWonLostStatistics() {
        // Generate statistics
        List<TeamStatistic> stats = decayingWonLostModel.generate(testSeason);

        // Get statistics for each day to verify decay
        Map<String, TeamStatistic> day1Stats = getStatsForDate(stats, LocalDate.of(2025, 1, 1));
        Map<String, TeamStatistic> day2Stats = getStatsForDate(stats, LocalDate.of(2025, 1, 2));
        Map<String, TeamStatistic> day3Stats = getStatsForDate(stats, LocalDate.of(2025, 1, 3));
        Map<String, TeamStatistic> day4Stats = getStatsForDate(stats, LocalDate.of(2025, 1, 4));

        double DECAY = 0.975;

        // Alpha: Day 1 win=1, Day 2 win=1*0.975, Day 3 loss (win=1*0.975*0.975, loss=1), Day 4 (win decays, loss decays)
        assertStat(day1Stats, alpha, "DECAYING_WINS", 1.0);
        assertStat(day1Stats, alpha, "DECAYING_LOSSES", 0.0);
        assertStat(day1Stats, alpha, "DECAYING_WIN_PCT", 1.0);

        assertStat(day2Stats, alpha, "DECAYING_WINS", 1.0 * DECAY);
        assertStat(day2Stats, alpha, "DECAYING_LOSSES", 0.0);

        assertStat(day3Stats, alpha, "DECAYING_WINS", 1.0 * DECAY * DECAY);
        assertStat(day3Stats, alpha, "DECAYING_LOSSES", 1.0);

        double alphaDay4Wins = 1.0 * DECAY * DECAY * DECAY;
        double alphaDay4Losses = 1.0 * DECAY;
        assertStat(day4Stats, alpha, "DECAYING_WINS", alphaDay4Wins);
        assertStat(day4Stats, alpha, "DECAYING_LOSSES", alphaDay4Losses);
        assertStat(day4Stats, alpha, "DECAYING_WIN_PCT", alphaDay4Wins / (alphaDay4Wins + alphaDay4Losses));
    }

    @Test
    void testRpiStatistics() {
        // Generate statistics
        List<TeamStatistic> stats = rpiModel.generate(testSeason);

        // Get final day statistics
        Map<String, TeamStatistic> finalStats = getStatsForDate(stats, LocalDate.of(2025, 1, 4));

        // Manual RPI calculation:
        // Alpha: WP = 0.5 (1-1)
        //   Opponents: Beta (1-1, WP=0.5), Gamma (2-0, WP=1.0) -> OWP = 0.75
        //   Beta's opponents: Alpha (0.5), Delta (0.0)
        //   Gamma's opponents: Delta (0.0), Alpha (0.5)
        //   OOWP = average of all opponent-opponent WPs = (0.5 + 0.0 + 0.0 + 0.5) / 4 = 0.25
        //   RPI = 0.25*0.5 + 0.50*0.75 + 0.25*0.25 = 0.125 + 0.375 + 0.0625 = 0.5625

        assertStat(finalStats, alpha, "RPI_WP", 0.5);
        assertStat(finalStats, alpha, "RPI_OWP", 0.75);
        assertStat(finalStats, alpha, "RPI_OOWP", 0.25);
        assertStat(finalStats, alpha, "RPI", 0.5625);

        // Gamma: WP = 1.0 (2-0)
        //   Opponents: Delta (0-2, WP=0.0), Alpha (1-1, WP=0.5) -> OWP = 0.25
        //   Delta's opponents: Gamma (1.0), Beta (0.5)
        //   Alpha's opponents: Beta (0.5), Gamma (1.0)
        //   OOWP = average of all opponent-opponent WPs = (1.0 + 0.5 + 0.5 + 1.0) / 4 = 0.75
        //   RPI = 0.25*1.0 + 0.50*0.25 + 0.25*0.75 = 0.25 + 0.125 + 0.1875 = 0.5625

        assertStat(finalStats, gamma, "RPI_WP", 1.0);
        assertStat(finalStats, gamma, "RPI_OWP", 0.25);
        assertStat(finalStats, gamma, "RPI_OOWP", 0.75);
        assertStat(finalStats, gamma, "RPI", 0.5625);
    }

    @Test
    void testNormalizedPointsStatistics() {
        // Generate statistics
        List<TeamStatistic> stats = normalizedPointsModel.generate(testSeason);

        // Get statistics for different days to verify normalization
        Map<String, TeamStatistic> day1Stats = getStatsForDate(stats, LocalDate.of(2025, 1, 1));
        Map<String, TeamStatistic> day2Stats = getStatsForDate(stats, LocalDate.of(2025, 1, 2));
        Map<String, TeamStatistic> day3Stats = getStatsForDate(stats, LocalDate.of(2025, 1, 3));

        // Day 1: First game Alpha 80 vs Beta 70
        // Both teams have no opponent history, so normalized scores = 0.0
        assertStat(day1Stats, alpha, "NORM_PF_AVG", 0.0);
        assertStat(day1Stats, alpha, "NORM_PA_AVG", 0.0);
        assertStat(day1Stats, beta, "NORM_PF_AVG", 0.0);
        assertStat(day1Stats, beta, "NORM_PA_AVG", 0.0);

        // Day 2: Game 2 - Gamma 90 vs Delta 85
        // Gamma and Delta also have no opponent history, so normalized scores = 0.0
        // Alpha and Beta stats remain the same as Day 1
        assertStat(day2Stats, gamma, "NORM_PF_AVG", 0.0);
        assertStat(day2Stats, gamma, "NORM_PA_AVG", 0.0);

        // Day 3: Game 3 - Alpha 75 vs Gamma 80
        // Alpha's normalized PF: (75 - Gamma's PA mean) / Gamma's PA sd
        //   Gamma allowed 85 to Delta, mean=85, sd=0 → (75-85) = -10
        // Gamma's normalized PF: (80 - Alpha's PA mean) / Alpha's PA sd
        //   Alpha allowed 70 to Beta, mean=70, sd=0 → (80-70) = 10
        // Alpha's average: (0.0 + (-10)) / 2 = -5.0
        assertStat(day3Stats, alpha, "NORM_PF_AVG", -5.0);
        // Gamma's average: (0.0 + 10) / 2 = 5.0
        assertStat(day3Stats, gamma, "NORM_PF_AVG", 5.0);
    }

    // Helper methods

    private Team createTeam(String name, String abbreviation, String slug, String longName, String espnId) {
        Team team = new Team();
        team.setName(name);
        team.setNickname(name);
        team.setAbbreviation(abbreviation);
        team.setSlug(slug);
        team.setLongName(longName);
        team.setEspnId(espnId);
        team.setPrimaryColor("#000000");
        team.setSecondaryColor("#FFFFFF");
        return teamRepository.save(team);
    }

    private void createGame(String espnId, LocalDate date, Team homeTeam, Team awayTeam, int homeScore, int awayScore) {
        Game game = new Game();
        game.setEspnId(espnId);
        game.setSeason(testSeason);
        game.setDate(date);
        game.setIndexDate(date);
        game.setHomeTeam(homeTeam);
        game.setAwayTeam(awayTeam);
        game.setHomeScore(homeScore);
        game.setAwayScore(awayScore);
        game.setStatus("FINAL");
        game.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        gameRepository.save(game);
    }

    private Map<String, TeamStatistic> getStatsForDate(List<TeamStatistic> stats, LocalDate date) {
        return stats.stream()
                .filter(s -> s.getStatisticDate().equals(date))
                .collect(Collectors.toMap(
                        s -> s.getTeam().getName() + "_" + s.getStatisticType().getCode(),
                        s -> s
                ));
    }

    private void assertStat(Map<String, TeamStatistic> stats, Team team, String statKey, double expectedValue) {
        String key = team.getName() + "_" + statKey;
        assertThat(stats).containsKey(key);
        TeamStatistic stat = stats.get(key);
        assertThat(stat.getNumericValue().doubleValue())
                .as("Statistic %s for team %s", statKey, team.getName())
                .isCloseTo(expectedValue, within(0.001));
    }
}
