package com.fijimf.deepfij.controller.admin;

import com.fijimf.deepfij.model.User;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.service.ScheduleService;
import com.fijimf.deepfij.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<ScheduleService.SeasonStatus>>> status(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getStatus().seasons()));
    }


    @PostMapping("/new")
    public ResponseEntity<ApiResponse<ScheduleService.ScheduleStatus>> loadSeason(@RequestParam int seasonYear) {
        if (scheduleService.createSchedule(seasonYear)) {
            return ResponseEntity.ok(ApiResponse.success(scheduleService.getStatus()));
        } else {
            return ResponseEntity.status(400).body(ApiResponse.error("Unable to create season for year: "+seasonYear+".  Please check logs for details."));
        }
    }

    @PostMapping("/drop/{seasonYear}")
    public ResponseEntity<ApiResponse<ScheduleService.ScheduleStatus>> dropSeason(@PathVariable int seasonYear) {
        int count = scheduleService.dropSeason(seasonYear);
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getStatus()));
    }

    @GetMapping("/refresh/{seasonYear}")
    public ResponseEntity<ApiResponse<ScheduleService.ScheduleStatus>> fetchGames(@PathVariable int seasonYear) {
        scheduleService.refresh(seasonYear);
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getStatus()));
    }

}