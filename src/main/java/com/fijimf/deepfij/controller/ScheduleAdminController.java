package com.fijimf.deepfij.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fijimf.deepfij.auth.util.JwtUtil;
import com.fijimf.deepfij.model.User;
import com.fijimf.deepfij.model.schedule.Conference;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repo.UserRepository;
import com.fijimf.deepfij.service.ScheduleService;
import com.fijimf.deepfij.service.StatisticalService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/schedule/admin")
public class ScheduleAdminController {

    private final ScheduleService scheduleService;
    private final StatisticalService statisticalService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Autowired
    public ScheduleAdminController(ScheduleService scheduleService, StatisticalService statisticalService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.scheduleService = scheduleService;
        this.statisticalService = statisticalService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @GetMapping("/status")
    public ResponseEntity<ScheduleService.ScheduleStatus> status(HttpServletRequest request) {
        return ResponseEntity.ok(scheduleService.getStatus());
    }



    @GetMapping("/loadConferences")
    public ResponseEntity<List<Conference>> loadConferences(HttpServletRequest httpServletRequest) {
        User user = getUser(httpServletRequest);
        List<Conference> conferences = scheduleService.loadConferences(user);
        return ResponseEntity.ok(conferences);
    }

    @GetMapping("/dropConferences")
    public ResponseEntity<Integer> dropConferences(HttpServletRequest httpServletRequest) {
        User user = getUser(httpServletRequest);
        int count = scheduleService.dropConferences(user);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/loadTeams")
    public ResponseEntity<List<Team>> loadTeams(HttpServletRequest httpServletRequest) {
        User user = getUser(httpServletRequest);
        List<Team> teams = scheduleService.loadTeams(user);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/dropTeams")
    public ResponseEntity<Integer> dropTeams(HttpServletRequest httpServletRequest) {
        User user = getUser(httpServletRequest);
        int count = scheduleService.dropTeams(user);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/loadSeason")
    public ResponseEntity<ScheduleService.ScheduleStatus> loadSeason(HttpServletRequest httpServletRequest, @RequestParam int seasonYear) {
        User user = getUser(httpServletRequest);
        scheduleService.createSchedule(seasonYear, user);
        return ResponseEntity.ok(scheduleService.getStatus());
    }

    @GetMapping("/dropSeason")
    public ResponseEntity<Integer> dropSeason(HttpServletRequest httpServletRequest, @RequestParam int seasonYear) {
        User user = getUser(httpServletRequest);
        int count = scheduleService.dropSeason(seasonYear, user);
        return ResponseEntity.ok(count);
    }
    @GetMapping("/loadGames")
    public ResponseEntity<List<Game>> fetchGames(@RequestParam int seasonYear, @RequestParam String date, HttpServletRequest httpServletRequest) {
        // Parse the date string into LocalDate
        LocalDate localDate = LocalDate.parse(date);
        User user = getUser(httpServletRequest);
        List<Game> games = scheduleService.fetchGames(seasonYear, localDate, user);
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

    private User getUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return userRepository.findByUsername( jwtUtil.extractUsername(token));
    }
}