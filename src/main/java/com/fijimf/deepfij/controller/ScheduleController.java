package com.fijimf.deepfij.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fijimf.deepfij.service.TournamentBuilder;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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

    @Autowired
    public ScheduleController(SeasonRepository seasonRepository, TeamRepository teamRepository, GameRepository gameRepository, TournamentBuilder tournamentBuilder) {
        this.seasonRepository = seasonRepository;
        this.teamRepository = teamRepository;
        this.gameRepository = gameRepository;
        this.tournamentBuilder = tournamentBuilder;
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
    public ResponseEntity<ApiResponse<TournamentBuilder.Tournament>> getTournament( @RequestParam(required = false) Integer year) {
        Season season;
        if (year == null) {
            season = seasonRepository.findFirstByOrderByYearDesc(); // Fetch most recent season
        } else {
            season = seasonRepository.findByYear(year).getFirst(); // Fetch season by year
        }
        if (season == null) return ResponseEntity.status(404).body(ApiResponse.error("Season not found"));

       return ResponseEntity.ok(ApiResponse.success(tournamentBuilder.build(season)));


    }

    @Cacheable(value = "gamesByDate", key = "#season + '-' + #yyyymmdd")
    @GetMapping("/games/{season}/{yyyymmdd}")
    public ResponseEntity<ApiResponse<GamesByDateDTO>> getGamesByDate(@PathVariable int season, @PathVariable String yyyymmdd) {
        logger.info("Fetching games for season {} on date {}", season, yyyymmdd);
        
        LocalDate date;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            date = LocalDate.parse(yyyymmdd, formatter);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error("Invalid date format. Expected yyyyMMdd"));
        }
        
        Season seasonEntity = seasonRepository.findByYear(season).stream()
                .findFirst()
                .orElse(null);
        
        if (seasonEntity == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("Season not found: " + season));
        }
        
        List<Game> games = gameRepository.findBySeasonAndIndexDate(seasonEntity, date);
        
        List<GameDTO> gameDTOs = games.stream()
                .map(game -> GameDTO.fromGame(game, null))
                .toList();
        
        GamesByDateDTO response = new GamesByDateDTO(season, date, gameDTOs);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Cacheable(value = "gamesBySeason", key = "#season")
    @GetMapping("/games/{season}")
    public ResponseEntity<ApiResponse<GamesByDateDTO>> getGamesBySeason(@PathVariable int season) {
        logger.info("Fetching games for season {}", season);
        
        Season seasonEntity = seasonRepository.findByYear(season).stream()
                .findFirst()
                .orElse(null);
        
        if (seasonEntity == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("Season not found: " + season));
        }
        
        LocalDate today = LocalDate.now();
        LocalDate targetDate;
        
        // Check if today falls within the season
        if (!today.isBefore(seasonEntity.getStartDate()) && !today.isAfter(seasonEntity.getEndDate())) {
            targetDate = today;
        } else {
            // Use the last date with completed games
            targetDate = gameRepository.findLastCompletedGameDate(seasonEntity);
            if (targetDate == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("No completed games found for season: " + season));
            }
        }
        
        List<Game> games = gameRepository.findBySeasonAndIndexDate(seasonEntity, targetDate);
        
        List<GameDTO> gameDTOs = games.stream()
                .map(game -> GameDTO.fromGame(game, null))
                .toList();
        
        GamesByDateDTO response = new GamesByDateDTO(season, targetDate, gameDTOs);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Cacheable(value = "gamesLatest")
    @GetMapping("/games")
    public ResponseEntity<ApiResponse<GamesByDateDTO>> getGamesForLatestSeason() {
        logger.info("Fetching games for latest season");
        
        Season latestSeason = seasonRepository.findFirstByOrderByYearDesc();
        if (latestSeason == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("No seasons found"));
        }
        
        LocalDate today = LocalDate.now();
        LocalDate targetDate;
        
        // Check if today falls within the season
        if (!today.isBefore(latestSeason.getStartDate()) && !today.isAfter(latestSeason.getEndDate())) {
            targetDate = today;
        } else {
            // Use the last date with completed games
            targetDate = gameRepository.findLastCompletedGameDate(latestSeason);
            if (targetDate == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("No completed games found for latest season: " + latestSeason.getYear()));
            }
        }
        
        List<Game> games = gameRepository.findBySeasonAndIndexDate(latestSeason, targetDate);
        
        List<GameDTO> gameDTOs = games.stream()
                .map(game -> GameDTO.fromGame(game, null))
                .toList();
        
        GamesByDateDTO response = new GamesByDateDTO(latestSeason.getYear(), targetDate, gameDTOs);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
