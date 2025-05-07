package com.fijimf.deepfij.controller;

import com.fijimf.deepfij.model.schedule.Conference;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.service.ScheduleService;
import com.fijimf.deepfij.service.StatisticalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedule/admin")
public class ScheduleAdminController {

    private final ScheduleService scheduleService;
    private final StatisticalService statisticalService;

    @Autowired
    public ScheduleAdminController(ScheduleService scheduleService, StatisticalService statisticalService) {
        this.scheduleService = scheduleService;
        this.statisticalService = statisticalService;
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

    @GetMapping("/loadSeason")
    public ResponseEntity<ScheduleService.ScheduleStatus> loadSeason(@RequestParam int seasonYear) {
        scheduleService.createSchedule(seasonYear);
        return ResponseEntity.ok(scheduleService.getStatus());
    }

    @GetMapping("/loadGames")
    public ResponseEntity<List<Game>> fetchGames(@RequestParam int seasonYear, @RequestParam String date) {
        // Parse the date string into LocalDate
        LocalDate localDate = LocalDate.parse(date);
        List<Game> games = scheduleService.fetchGames(seasonYear, localDate);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/runModel")
    public ResponseEntity<Map<LocalDate,Integer>> runModel(@RequestParam int seasonYear, @RequestParam String model) {
        List<TeamStatistic> teamStatistics = statisticalService.generateStatistics(Integer.toString(seasonYear), model);
        Map<LocalDate, Integer> countByDate = teamStatistics
                .stream()
                .collect(Collectors.groupingBy(TeamStatistic::getStatisticDate))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
        return ResponseEntity.ok(countByDate);
    }

}