package com.fijimf.deepfij.controller.admin;

import com.fijimf.deepfij.model.User;
import com.fijimf.deepfij.model.schedule.Conference;
import com.fijimf.deepfij.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/schedule/conference")
public class ConferenceAdminController {

    private final ScheduleService scheduleService;
    private final ControllerUtil controllerUtil;

    @Autowired
    public ConferenceAdminController(ScheduleService scheduleService, ControllerUtil controllerUtil) {
        this.scheduleService = scheduleService;
        this.controllerUtil = controllerUtil;

    }

    @GetMapping("/")
    public ResponseEntity<ScheduleService.ConferenceStatus> status() {
        return ResponseEntity.ok(scheduleService.getConferenceStatus());
    }

    @GetMapping("/load")
    public ResponseEntity<ScheduleService.ConferenceStatus> loadConferences(HttpServletRequest httpServletRequest) {
        User user = controllerUtil.getUser(httpServletRequest);
        List<Conference> conferences = scheduleService.loadConferences(user);
        return ResponseEntity.ok(scheduleService.getConferenceStatus());
    }

    @PostMapping("/drop")
    public ResponseEntity<ScheduleService.ConferenceStatus> dropConferences(HttpServletRequest httpServletRequest) {
        User user = controllerUtil.getUser(httpServletRequest);
        int count = scheduleService.dropConferences(user);
        return ResponseEntity.ok(scheduleService.getConferenceStatus());
    }

}