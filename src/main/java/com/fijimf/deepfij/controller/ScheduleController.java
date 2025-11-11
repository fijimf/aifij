package com.fijimf.deepfij.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fijimf.deepfij.model.dto.gamedetail.GameDetail;
import com.fijimf.deepfij.service.GameDetailService;
import com.fijimf.deepfij.service.ScheduleService;
import com.fijimf.deepfij.service.TournamentBuilder;
import jakarta.persistence.EntityNotFoundException;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fijimf.deepfij.model.dto.TeamPage;
import com.fijimf.deepfij.model.dto.TeamsPage;
import com.fijimf.deepfij.model.dto.TeamsByConferencePage;
import com.fijimf.deepfij.model.dto.GamesByDateDTO;
import com.fijimf.deepfij.model.dto.GameDTO;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.repo.SeasonRepository;
import com.fijimf.deepfij.repo.TeamRepository;
import com.fijimf.deepfij.repo.GameRepository;
import com.fijimf.deepfij.response.ApiResponse;

@RestController
public class ScheduleController {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    private final SeasonRepository seasonRepository; // Inject SeasonRepository
    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;
    private final TournamentBuilder tournamentBuilder;
    private final ScheduleService scheduleService;
    private final GameDetailService gameDetailService;

    @Autowired
    public ScheduleController(SeasonRepository seasonRepository, TeamRepository teamRepository, GameRepository gameRepository, TournamentBuilder tournamentBuilder, ScheduleService scheduleService, GameDetailService gameDetailService) {
        this.seasonRepository = seasonRepository;
        this.teamRepository = teamRepository;
        this.gameRepository = gameRepository;
        this.tournamentBuilder = tournamentBuilder;
        this.scheduleService = scheduleService;
        this.gameDetailService = gameDetailService;
    }

    @Cacheable(value = "teamPages", key = "#year + '-' + #teamId")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<TeamPage>> getTeamData(@PathVariable Long teamId, @RequestParam(required = false) Integer year) {

        logger.info(teamId.toString());
        Season season;
        if (year == null) {
            season = seasonRepository.findFirstByOrderByYearDesc(); // Fetch most recent season
        } else {
            season = seasonRepository.findByYear(year).getFirst(); // Fetch season by year
        }
        if (season == null) return ResponseEntity.status(404).body(ApiResponse.error("Season not found"));
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) return ResponseEntity.status(404).body(ApiResponse.error("Team not found"));
        return ResponseEntity.ok(ApiResponse.success(TeamPage.create(team, season, seasonRepository.findAll().stream().map(Season::getYear).toList())));

    }

    @Cacheable(value = "teamPages")
    @GetMapping("/teams")
    public ResponseEntity<ApiResponse<TeamsPage>> getTeamData() {

        logger.info("Fetching teams");
        List<Team> teams = teamRepository.findAll();
        Season season = seasonRepository.findFirstByOrderByYearDesc(); // Fetch most recent season
        if (season == null) return ResponseEntity.status(404).body(ApiResponse.error("Season not found"));
        return ResponseEntity.ok(ApiResponse.success(TeamsPage.create(teams, season)));

    }

    @Cacheable(value = "teamsByConferencePages", key = "#year")
    @GetMapping("/teams-by-conference")
    public ResponseEntity<ApiResponse<TeamsByConferencePage>> getTeamsByConferenceData(@RequestParam(required = false) Integer year) {

        logger.info("Fetching teams by conference for year: {}", year);
        Season season;
        if (year == null) {
            season = seasonRepository.findFirstByOrderByYearDesc(); // Fetch most recent season
        } else {
            List<Season> seasons = seasonRepository.findByYear(year);
            if (seasons.isEmpty()) {
                return ResponseEntity.status(404).body(ApiResponse.error("Season not found for year: " + year));
            }
            season = seasons.getFirst(); // Fetch season by year
        }
        if (season == null) return ResponseEntity.status(404).body(ApiResponse.error("Season not found"));

        List<Team> teams = teamRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(TeamsByConferencePage.create(teams, season)));

    }

    @GetMapping("/tournament")
    public ResponseEntity<ApiResponse<TournamentBuilder.Tournament>> getTournament(@RequestParam(required = false) Integer year) {
        Season season;
        if (year == null) {
            season = seasonRepository.findFirstByOrderByYearDesc(); // Fetch most recent season
        } else {
            season = seasonRepository.findByYear(year).getFirst(); // Fetch season by year
        }
        if (season == null) return ResponseEntity.status(404).body(ApiResponse.error("Season not found"));

        return ResponseEntity.ok(ApiResponse.success(tournamentBuilder.build(season)));


    }

    @Cacheable(value = "gamesByDate", key = "#yyyymmdd")
    @GetMapping({"/games/{yyyymmdd}", "/games"})
    public ResponseEntity<ApiResponse<GamesByDateDTO>> getGamesByDate(@PathVariable(required = false) String yyyymmdd) {
        if (yyyymmdd==null || yyyymmdd.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(scheduleService.fetchGamesByDateDTO(LocalDate.now())));
        } else {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                return ResponseEntity.ok(ApiResponse.success(scheduleService.fetchGamesByDateDTO(LocalDate.parse(yyyymmdd, formatter))));
            } catch (Exception e) {
                return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
            }
        }
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<ApiResponse<GameDetail>> getGameDetail(@PathVariable Long gameId) {
        try {
            logger.info("Fetching game detail for game ID: {}", gameId);
            GameDetail gameDetail = gameDetailService.getGameDetail(gameId);
            return ResponseEntity.ok(ApiResponse.success("Game details retrieved successfully", gameDetail));
        } catch (EntityNotFoundException e) {
            logger.warn("Game not found with id: {}", gameId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Game not found with id: " + gameId));
        } catch (Exception e) {
            logger.error("Error retrieving game details for game ID: {}", gameId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving game details"));
        }
    }
}

