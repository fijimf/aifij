package com.fijimf.deepfij.controller.admin;

import com.fijimf.deepfij.model.User;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/schedule/season")
public class SeasonAdminController {

    private final ScheduleService scheduleService;
    private final ControllerUtil controllerUtil;

    @Autowired
    public SeasonAdminController(ScheduleService scheduleService, ControllerUtil controllerUtil) {
        this.scheduleService = scheduleService;
        this.controllerUtil = controllerUtil;
    }

    @GetMapping("/")
    public ResponseEntity<List<ScheduleService.SeasonStatus>> status(HttpServletRequest request) {
        return ResponseEntity.ok(scheduleService.getStatus().seasons());
    }


    @GetMapping("/season/new")
    public ResponseEntity<ScheduleService.ScheduleStatus> loadSeason(HttpServletRequest httpServletRequest, @RequestParam int seasonYear) {
        User user = controllerUtil.getUser(httpServletRequest);
        scheduleService.createSchedule(seasonYear, user);
        return ResponseEntity.ok(scheduleService.getStatus());
    }

    @PostMapping("/drop/{seasonYear}")
    public ResponseEntity<Integer> dropSeason(HttpServletRequest httpServletRequest, @RequestParam int seasonYear) {
        User user = controllerUtil.getUser(httpServletRequest);
        int count = scheduleService.dropSeason(seasonYear, user);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/refresh/{seasonYear}")
    public ResponseEntity<List<Game>> fetchGames(@PathVariable int seasonYear, @RequestParam String date, HttpServletRequest httpServletRequest) {
        // Parse the date string into LocalDate
        LocalDate localDate = LocalDate.parse(date);
        User user = controllerUtil.getUser(httpServletRequest);
        List<Game> games = scheduleService.fetchGames(seasonYear, localDate, user);
        return ResponseEntity.ok(games);
    }


}