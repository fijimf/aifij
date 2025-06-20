package com.fijimf.deepfij.controller.admin;

import com.fijimf.deepfij.model.User;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
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


    @PostMapping("/new")
    public ResponseEntity<ScheduleService.ScheduleStatus> loadSeason(HttpServletRequest httpServletRequest, @RequestParam int seasonYear) {
        User user = controllerUtil.getUser(httpServletRequest);
        scheduleService.createSchedule(seasonYear, user);
        return ResponseEntity.ok(scheduleService.getStatus());
    }

    @PostMapping("/drop/{seasonYear}")
    public ResponseEntity<ScheduleService.ScheduleStatus> dropSeason(@PathVariable int seasonYear,  HttpServletRequest httpServletRequest) {
        User user = controllerUtil.getUser(httpServletRequest);
        int count = scheduleService.dropSeason(seasonYear, user);
        return ResponseEntity.ok(scheduleService.getStatus());
    }

    @GetMapping("/refresh/{seasonYear}")
    public ResponseEntity<ScheduleService.ScheduleStatus> fetchGames(@PathVariable int seasonYear, HttpServletRequest httpServletRequest) {
        // Parse the date string into LocalDate
        User user = controllerUtil.getUser(httpServletRequest);
        scheduleService.refresh(seasonYear, user);
        return ResponseEntity.ok(scheduleService.getStatus());
    }

}