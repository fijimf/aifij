package com.fijimf.deepfij.controller;

import java.util.List;

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
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.repo.SeasonRepository;
import com.fijimf.deepfij.repo.TeamRepository;

@RestController
public class ScheduleController {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    @Autowired
    private SeasonRepository seasonRepository; // Inject SeasonRepository
    @Autowired
    private TeamRepository teamRepository;

    @Cacheable(value = "teamPages", key = "#year + '-' + #teamId")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<TeamPage> getTeamData(@PathVariable Long teamId, @RequestParam(required = false) Integer year) {

       logger.info(teamId.toString());
       Season season;
        if (year == null) {
            season = seasonRepository.findFirstByOrderByYearDesc(); // Fetch most recent season
        } else {
            season = seasonRepository.findByYear(year).getFirst(); // Fetch season by year
        }
        if (season == null) return ResponseEntity.notFound().build();
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(TeamPage.create(team, season));

    }

    @Cacheable(value = "teamPages")
    @GetMapping("/teams")
    public ResponseEntity<TeamsPage> getTeamData() {

        logger.info("Fetching teams");
        List<Team> teams = teamRepository.findAll();
        Season season = seasonRepository.findFirstByOrderByYearDesc(); // Fetch most recent season
        if (season == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(TeamsPage.create(teams, season));

    }
}
