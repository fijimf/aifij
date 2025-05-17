package com.fijimf.deepfij.controller;

import com.fijimf.deepfij.model.dto.StatSummaryPage;
import com.fijimf.deepfij.model.dto.TeamsPage;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.repo.SeasonRepository;
import com.fijimf.deepfij.service.impl.StatisticServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StatsController {
    private static final Logger logger = LoggerFactory.getLogger(StatsController.class);

    @Autowired
    private StatisticServiceImpl statisticService; // Inject SeasonRepository
    @Autowired
    private SeasonRepository seasonRepository; // Inject SeasonRepository


    @GetMapping("/stats/{statName}/summary")
    public ResponseEntity<StatSummaryPage> getStatSummary(@PathVariable String statName, @RequestParam(required = false) Integer year) {


        Season season;
        if (year == null) {
            season = seasonRepository.findFirstByOrderByYearDesc(); // Fetch most recent season
        } else {
            season = seasonRepository.findByYear(year).getFirst(); // Fetch season by year
        }
        if (season == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(statisticService.getStatSummaryPage(season.getYear(), statName));

    }

}
