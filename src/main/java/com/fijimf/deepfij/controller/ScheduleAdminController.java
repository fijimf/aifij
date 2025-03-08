package com.fijimf.deepfij.controller;

import com.fijimf.deepfij.model.schedule.Conference;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/schedule/admin")
public class ScheduleAdminController {

    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleAdminController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/status")
    public ResponseEntity<ScheduleService.ScheduleStatus> status() {
        return ResponseEntity.ok(scheduleService.getStatus());
    }

    @GetMapping("/loadConferences")
    public ResponseEntity<List<Conference>> loadConferences() {
        List<Conference> conferences = scheduleService.loadConferences();
        return ResponseEntity.ok(conferences);
    }

    @GetMapping("/loadTeams")
    public ResponseEntity<List<Team>> loadTeams() {
        List<Team> teams = scheduleService.loadTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/loadConferenceMappings")
    public ResponseEntity<List<Game>> loadConferenceMappings(@RequestParam String seasonYear, @RequestParam String date) {
        // Parse the date string into LocalDate
        LocalDate localDate = LocalDate.parse(date);
        List<Game> games = scheduleService.fetchGames(localDate);
        return ResponseEntity.ok(games);
    }
    @GetMapping("/loadGames")
    public ResponseEntity<List<Game>> fetchGames(@RequestParam String seasonYear, @RequestParam String date) {
        // Parse the date string into LocalDate
        LocalDate localDate = LocalDate.parse(date);
        List<Game> games = scheduleService.fetchGames(localDate);
        return ResponseEntity.ok(games);
    }

}