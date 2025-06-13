package com.fijimf.deepfij.controller;

import com.fijimf.deepfij.ml.MachineLearningService;
import com.fijimf.deepfij.ml.ModelData;
import com.fijimf.deepfij.ml.Models;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.repo.SeasonRepository;
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
     * Returns a list of available model definitions.
     * 
     * @return Map of model keys to model names
     */
    @GetMapping("/models")
    public ResponseEntity<List<String>> getAvailableModels() {
        return ResponseEntity.ok(machineLearningService.getKeys());
    }

    /**
     * Returns model data for a specific model and season.
     * 
     * @param key The model key
     * @param year The (optional) season year
     * @return ResponseEntity containing the model data, or 404 if the model or season was not found
     */
    @GetMapping("/train/{key}")
    public ResponseEntity<Map<String, String>> getModelData(
            @PathVariable String key, @RequestParam(required = false) Integer year) {
        ModelData modelData = machineLearningService.getModelData(key, year);
        String url = "http://127.0.0.1:5000/train";
        return ResponseEntity.ok(restTemplate.postForObject(url, modelData, Map.class));
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
    public ResponseEntity<Map<String, double[][]>> getModelData(
            @PathVariable String key,
            @PathVariable Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {


        return ResponseEntity.ok(null);
    }

    /**
     * Returns information about a specific model.
     * 
     * @param modelKey The model key
     * @return ResponseEntity containing the model information, or 404 if the model was not found
     */
    @GetMapping("/models/{modelKey}")
    public ResponseEntity<Map<String, String>> getModelInfo(@PathVariable String modelKey) {
        return null;
    }
}
