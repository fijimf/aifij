package com.fijimf.deepfij.controller.admin;

import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.service.StatisticalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/stats")
public class StatisticsAdminController {

    private final StatisticalService statisticalService;

    @Autowired
    public StatisticsAdminController(StatisticalService statisticalService) {
        this.statisticalService = statisticalService;
    }

    @GetMapping("/")
    public ResponseEntity<StatisticalService.StatisticsStatus> status(){
      return ResponseEntity.ok( statisticalService.getStatisticStatus());
    }

    @PostMapping("/{model}/run")
    public ResponseEntity<StatisticalService.StatisticsStatus> runModel(@RequestParam int season, @PathVariable String model) {
        List<TeamStatistic> teamStatistics = statisticalService.generateStatistics(Integer.toString(season), model);
        Map<LocalDate, Integer> countByDate = teamStatistics
                .stream()
                .collect(Collectors.groupingBy(TeamStatistic::getStatisticDate))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
        return ResponseEntity.ok( statisticalService.getStatisticStatus());
    }

    @GetMapping("/models")
    public ResponseEntity<List<String>> listStatModels() {
        return ResponseEntity.ok(statisticalService.modelKeys());
    }

}