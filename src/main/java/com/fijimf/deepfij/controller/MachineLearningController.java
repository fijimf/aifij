package com.fijimf.deepfij.controller;

import com.fijimf.deepfij.ml.MachineLearningService;
import com.fijimf.deepfij.repo.SeasonRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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

}
