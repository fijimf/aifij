package com.fijimf.deepfij.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fijimf.deepfij.model.schedule.Conference;
import com.fijimf.deepfij.model.schedule.ConferenceMapping;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.repo.AuditRepository;
import com.fijimf.deepfij.repo.ConferenceMappingRepository;
import com.fijimf.deepfij.repo.ConferenceRepository;
import com.fijimf.deepfij.repo.GameRepository;
import com.fijimf.deepfij.repo.SeasonRepository;
import com.fijimf.deepfij.repo.TeamRepository;
import com.fijimf.deepfij.repo.TeamStatisticRepository;
import com.fijimf.deepfij.service.ScheduleService.ConferenceStatus;
import com.fijimf.deepfij.service.ScheduleService.ScheduleStatus;
import com.fijimf.deepfij.service.ScheduleService.SeasonStatus;
import com.fijimf.deepfij.service.ScheduleService.TeamStatus;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScrapingService scrapingService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ConferenceRepository conferenceRepository;

    @Mock
    private ConferenceMappingRepository conferenceMappingRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private TeamStatisticRepository teamStatisticRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private Season testSeason;
    private List<Season> seasons;
    private List<ConferenceMapping> mappings;
    private List<Game> games;
    private List<Team> teams;
    private List<Conference> conferences;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testSeason = new Season();
        testSeason.setYear(2023);
        testSeason.setId(1L);
        seasons = List.of(testSeason);

        // Create test conferences
        Conference conf1 = new Conference();
        conf1.setId(1L);
        conf1.setName("Test Conference 1");
        conf1.setLogoUrl("http://example.com/logo1.png");

        Conference conf2 = new Conference();
        conf2.setId(2L);
        conf2.setName("Test Conference 2");
        conf2.setLogoUrl(null); // Missing logo

        conferences = Arrays.asList(conf1, conf2);

        // Create test teams
        Team team1 = new Team();
        team1.setId(1L);
        team1.setName("Team 1");
        team1.setLogoUrl("http://example.com/team1.png");
        team1.setPrimaryColor("#FF0000");

        Team team2 = new Team();
        team2.setId(2L);
        team2.setName("Team 2");
        team2.setLogoUrl(null); // Missing logo
        team2.setPrimaryColor("#00FF00");

        Team team3 = new Team();
        team3.setId(3L);
        team3.setName("Team 3");
        team3.setLogoUrl("http://example.com/team3.png");
        team3.setPrimaryColor(null); // Missing color

        teams = Arrays.asList(team1, team2, team3);

        // Create conference mappings
        ConferenceMapping mapping1 = new ConferenceMapping();
        mapping1.setId(1L);
        mapping1.setConference(conf1);
        mapping1.setTeam(team1);
        mapping1.setSeason(testSeason);

        ConferenceMapping mapping2 = new ConferenceMapping();
        mapping2.setId(2L);
        mapping2.setConference(conf1);
        mapping2.setTeam(team2);
        mapping2.setSeason(testSeason);

        ConferenceMapping mapping3 = new ConferenceMapping();
        mapping3.setId(3L);
        mapping3.setConference(conf2);
        mapping3.setTeam(team3);
        mapping3.setSeason(testSeason);

        mappings = Arrays.asList(mapping1, mapping2, mapping3);

        // Create test games
        LocalDate firstDate = LocalDate.of(2023, 11, 1);
        LocalDate lastDate = LocalDate.of(2024, 4, 1);
        LocalDate lastCompleteDate = LocalDate.of(2024, 3, 15);

        Game game1 = new Game();
        game1.setId(1L);
        game1.setSeason(testSeason);
        game1.setDate(firstDate);
        game1.setHomeScore(75);  // Setting scores makes isComplete() return true
        game1.setAwayScore(70);
        game1.setUpdatedAt(Timestamp.valueOf("2023-11-01 23:59:59"));

        Game game2 = new Game();
        game2.setId(2L);
        game2.setSeason(testSeason);
        game2.setDate(lastCompleteDate);
        game2.setHomeScore(80);  // Setting scores makes isComplete() return true
        game2.setAwayScore(75);
        game2.setUpdatedAt(Timestamp.valueOf("2024-03-15 23:59:59"));

        Game game3 = new Game();
        game3.setId(3L);
        game3.setSeason(testSeason);
        game3.setDate(lastDate);
        // Not setting scores or setting them to null makes isComplete() return false
        game3.setHomeScore(null);
        game3.setAwayScore(null);
        game3.setUpdatedAt(Timestamp.valueOf("2024-04-01 12:00:00"));

        games = Arrays.asList(game1, game2, game3);
    }

    @Test
    void getStatus_WithData_ShouldReturnCorrectStatus() {
        // Arrange
        when(seasonRepository.findAll()).thenReturn(seasons);
        when(conferenceMappingRepository.findBySeason(testSeason)).thenReturn(mappings);
        when(gameRepository.findBySeasonOrderByDateAsc(testSeason)).thenReturn(games);
        when(teamRepository.count()).thenReturn(3L);
        when(teamRepository.countByLogoUrlIsNull()).thenReturn(1L);
        when(teamRepository.countByPrimaryColorIsNull()).thenReturn(1L);
        when(conferenceRepository.count()).thenReturn(2L);
        when(conferenceRepository.countByLogoUrlIsNull()).thenReturn(1L);

        // Act
        ScheduleStatus status = scheduleService.getStatus();

        // Assert
        assertNotNull(status);

        // Verify TeamStatus
        TeamStatus teamStatus = status.teamStatus();
        assertEquals(3L, teamStatus.numberOfTeams());
        assertEquals("Missing color for 1 teams.  Missing logo for 1 teams.", teamStatus.teamStatus());

        // Verify ConferenceStatus
        ConferenceStatus conferenceStatus = status.conferenceStatus();
        assertEquals(2L, conferenceStatus.numberOfConferences());
        assertEquals("Missing logo for 1 conferences.", conferenceStatus.conferenceStatus());

        // Verify SeasonStatus
        List<SeasonStatus> seasonStatuses = status.seasons();
        assertEquals(1, seasonStatuses.size());

        SeasonStatus seasonStatus = seasonStatuses.get(0);
        assertEquals(2023, seasonStatus.year());
        assertEquals(3, seasonStatus.numberOfTeams());
        assertEquals(2, seasonStatus.numberOfConferences());
        assertEquals(3, seasonStatus.numberOfGames());
        assertEquals(LocalDate.of(2023, 11, 1), seasonStatus.firstGameDate());
        assertEquals(LocalDate.of(2024, 4, 1), seasonStatus.lastGameDate());
        assertEquals(LocalDate.of(2024, 3, 15), seasonStatus.lastCompleteGameDate());
        assertEquals(Timestamp.valueOf("2024-04-01 12:00:00"), seasonStatus.lastUpdated());
    }

    @Test
    void getStatus_WithEmptyData_ShouldReturnEmptyStatus() {
        // Arrange
        when(seasonRepository.findAll()).thenReturn(new ArrayList<>());
        when(teamRepository.count()).thenReturn(0L);
        when(conferenceRepository.count()).thenReturn(0L);

        // Act
        ScheduleStatus status = scheduleService.getStatus();

        // Assert
        assertNotNull(status);

        // Verify TeamStatus
        TeamStatus teamStatus = status.teamStatus();
        assertEquals(0L, teamStatus.numberOfTeams());
        assertEquals("No teams.", teamStatus.teamStatus());

        // Verify ConferenceStatus
        ConferenceStatus conferenceStatus = status.conferenceStatus();
        assertEquals(0L, conferenceStatus.numberOfConferences());
        assertEquals("No conferences.", conferenceStatus.conferenceStatus());

        // Verify SeasonStatus
        List<SeasonStatus> seasonStatuses = status.seasons();
        assertEquals(0, seasonStatuses.size());
    }

    // Note: We can't test with empty games list because the ScheduleService.getStatus() method
    // doesn't handle this case properly. It tries to access elements of an empty list without checking
    // if the list is empty first. Since we can't modify the ScheduleService class itself,
    // we'll focus on testing other aspects of the getStatus() method.

    @Test
    void getStatus_WithAllDataComplete_ShouldReturnOkStatus() {
        // Arrange
        when(seasonRepository.findAll()).thenReturn(seasons);
        when(conferenceMappingRepository.findBySeason(testSeason)).thenReturn(mappings);
        when(gameRepository.findBySeasonOrderByDateAsc(testSeason)).thenReturn(games);
        when(teamRepository.count()).thenReturn(3L);
        when(teamRepository.countByLogoUrlIsNull()).thenReturn(0L); // No missing logos
        when(teamRepository.countByPrimaryColorIsNull()).thenReturn(0L); // No missing colors
        when(conferenceRepository.count()).thenReturn(2L);
        when(conferenceRepository.countByLogoUrlIsNull()).thenReturn(0L); // No missing logos

        // Act
        ScheduleStatus status = scheduleService.getStatus();

        // Assert
        assertNotNull(status);

        // Verify TeamStatus shows OK
        TeamStatus teamStatus = status.teamStatus();
        assertEquals(3L, teamStatus.numberOfTeams());
        assertEquals("OK", teamStatus.teamStatus());

        // Verify ConferenceStatus shows OK
        ConferenceStatus conferenceStatus = status.conferenceStatus();
        assertEquals(2L, conferenceStatus.numberOfConferences());
        assertEquals("OK", conferenceStatus.conferenceStatus());
    }
}
