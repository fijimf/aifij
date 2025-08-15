package com.fijimf.deepfij.controller;

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
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.repo.SeasonRepository;
import com.fijimf.deepfij.repo.TeamRepository;
import com.fijimf.deepfij.response.ApiResponse;

@RestController
public class ScheduleController {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    private final SeasonRepository seasonRepository; // Inject SeasonRepository
    private final TeamRepository teamRepository;
    private final TournamentBuilder tournamentBuilder;

    @Autowired
    public ScheduleController(SeasonRepository seasonRepository, TeamRepository teamRepository, TournamentBuilder tournamentBuilder) {
        this.seasonRepository = seasonRepository;
        this.teamRepository = teamRepository;
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
}
