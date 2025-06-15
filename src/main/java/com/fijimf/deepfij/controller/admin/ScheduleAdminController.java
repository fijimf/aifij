package com.fijimf.deepfij.controller.admin;

import com.fijimf.deepfij.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/schedule")
public class ScheduleAdminController {


    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleAdminController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/")
    public ResponseEntity<ScheduleService.ScheduleStatus> status() {
        return ResponseEntity.ok(scheduleService.getStatus());
    }
}