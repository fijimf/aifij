package com.fijimf.deepfij.controller;

import com.fijimf.deepfij.ml.MachineLearningService;
import com.fijimf.deepfij.ml.ModelData;
import com.fijimf.deepfij.ml.Models;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.repo.SeasonRepository;
import com.fijimf.deepfij.response.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for machine learning functionality.
 * This controller exposes endpoints for accessing machine learning models and data.
 */
@RestController
@RequestMapping("/ml")
public class MachineLearningController {

    private final MachineLearningService machineLearningService;

    private final RestTemplate restTemplate;

    public MachineLearningController(
            MachineLearningService machineLearningService,
            SeasonRepository seasonRepository,
            RestTemplate restTemplate) {
        this.machineLearningService = machineLearningService;
        this.restTemplate = restTemplate;
    }

    /**
     * Returns model data for a specific model and season up to a specific date.
     * 
     * @param key The model key
     * @param year The season year
     * @param from The from date (inclusive)
     * @param to The to date (inclusive)
     * @return ResponseEntity containing the model data, or 404 if the model or season was not found
     */
    @GetMapping("/predict/{key}/{year}")
    public ResponseEntity<ApiResponse<Map<String, double[][]>>> getModelData(
            @PathVariable String key,
            @PathVariable Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {


        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Returns information about a specific model.
     * 
     * @param modelKey The model key
     * @return ResponseEntity containing the model information, or 404 if the model was not found
     */
    @GetMapping("/models/{modelKey}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getModelInfo(@PathVariable String modelKey) {
        return ResponseEntity.status(404).body(ApiResponse.error("Model info endpoint not implemented"));
    }
}
