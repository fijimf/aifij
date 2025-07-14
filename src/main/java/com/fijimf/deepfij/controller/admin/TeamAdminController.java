package com.fijimf.deepfij.controller.admin;

import com.fijimf.deepfij.auth.util.JwtUtil;
import com.fijimf.deepfij.model.User;
import com.fijimf.deepfij.repo.UserRepository;
import com.fijimf.deepfij.service.ScheduleService;
import com.fijimf.deepfij.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/schedule/team")
public class TeamAdminController {

    private final ScheduleService scheduleService;
    private final ControllerUtil controllerUtil;

    @Autowired
    public TeamAdminController(ScheduleService scheduleService, ControllerUtil controllerUtil) {
        this.scheduleService = scheduleService;
        this.controllerUtil = controllerUtil;

    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<ScheduleService.TeamStatus>> status() {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getTeamStatus()));
    }

    @PostMapping("/load")
    public ResponseEntity<ApiResponse<ScheduleService.TeamStatus>> loadTeams(HttpServletRequest httpServletRequest) {
        User user = controllerUtil.getUser(httpServletRequest);
        scheduleService.loadTeams(user);
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getTeamStatus()));
    }

    @PostMapping("/drop")
    public ResponseEntity<ApiResponse<ScheduleService.TeamStatus>> dropTeams(HttpServletRequest httpServletRequest) {
        User user = controllerUtil.getUser(httpServletRequest);
        scheduleService.dropTeams(user);
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getTeamStatus()));
    }


}