package com.fijimf.deepfij.controller;

import com.fijimf.deepfij.model.dto.StatSummaryPage;
import com.fijimf.deepfij.model.dto.TeamsPage;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.repo.SeasonRepository;
import com.fijimf.deepfij.service.impl.StatisticServiceImpl;
import com.fijimf.deepfij.response.ApiResponse;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class StatsController {
    private static final Logger logger = LoggerFactory.getLogger(StatsController.class);

    private final StatisticServiceImpl statisticService; // Inject SeasonRepository
    private final SeasonRepository seasonRepository; // Inject SeasonRepository

    @Autowired
    public StatsController(StatisticServiceImpl statisticService, SeasonRepository seasonRepository) {
        this.statisticService = statisticService;
        this.seasonRepository = seasonRepository;
    }


    @GetMapping("/stats/{statName}/summary")
    public ResponseEntity<ApiResponse<StatSummaryPage>> getStatSummary(@PathVariable String statName, @RequestParam(required = false) Integer year, @RequestParam(required = false) String yyyymmdd) {


        Season season;
        if (year == null) {
            season = seasonRepository.findFirstByOrderByYearDesc(); // Fetch most recent season
            if (yyyymmdd!= null) throw new IllegalArgumentException("Cannot specify yyyymmdd when year is not specified");
        } else {
            season = seasonRepository.findByYear(year).getFirst(); // Fetch season by year
        }
        if (season == null) return ResponseEntity.status(404).body(ApiResponse.error("Season not found"));
        if (yyyymmdd != null) {
            LocalDate d = LocalDate.parse(yyyymmdd, DateTimeFormatter.ofPattern("yyyyMMdd"));
            if (season.getStartDate().isAfter(d) || season.getEndDate().isBefore(d)) throw new IllegalArgumentException("Specified date is not within the season");
            return ResponseEntity.ok(ApiResponse.success(statisticService.getStatSummaryPage(statName, season, d)));
        } else {
            return ResponseEntity.ok(ApiResponse.success(statisticService.getStatSummaryPage(statName, season)));
        }

    }

}
