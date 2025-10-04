package com.fijimf.deepfij.controller;

import com.fijimf.deepfij.ml.MachineLearningService;
import com.fijimf.deepfij.model.dto.ModelRunsDTO;
import com.fijimf.deepfij.model.ml.Model;
import com.fijimf.deepfij.model.ml.ModelRun;
import com.fijimf.deepfij.repo.ModelRepository;
import com.fijimf.deepfij.repo.ModelRunRepository;
import com.fijimf.deepfij.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/models")
public class ModelController {
    private static final Logger logger = LoggerFactory.getLogger(ModelController.class);
    private final RestTemplate restTemplate;
    private final ModelRepository modelRepository;
    private final ModelRunRepository modelRunRepository;
    private final MachineLearningService mlService;

    public ModelController(RestTemplate restTemplate, ModelRepository modelRepository, ModelRunRepository modelRunRepository, MachineLearningService mlService) {
        this.restTemplate = restTemplate;
        this.modelRepository = modelRepository;
        this.modelRunRepository = modelRunRepository;
        this.mlService = mlService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Model>>> getAllModels() {
        logger.info("Fetching all models");
        try {
            List<Model> models = modelRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success(models));
        } catch (Exception e) {
            logger.error("Error fetching models", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch models: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ModelRunsDTO>> getModelById(@PathVariable Long id) {
        logger.info("Fetching model with id: {}", id);
        try {
            Optional<Model> model = modelRepository.findById(id);
            List<ModelRun> runs = modelRunRepository.findByModelId(id);
            if (model.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(ModelRunsDTO.create(model.get(),runs)));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching model with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch model: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/train")
    public ResponseEntity<ApiResponse<ModelRun>> trainModel(
            @PathVariable Long id,
            @RequestParam Map<String, String> queryParams) {
        logger.info("Training model with id: {} and params: {}", id, queryParams);
        try {
            ModelRun modelRun = mlService.prepareModelRun(id, queryParams);
            mlService.startModelTraining(id, modelRun.getId(), queryParams);
            return ResponseEntity.ok(ApiResponse.success(modelRun));
        } catch (Exception e) {
            logger.error("Error training model with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to train model: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/train/{runId}")
    public ResponseEntity<ApiResponse<ModelRun>> getModelRunDetails(
            @PathVariable Long id,
            @PathVariable Long runId) {
        logger.info("Fetching model run details for model id: {} and run id: {}", id, runId);
        try {
            Optional<ModelRun> modelRunOpt = modelRunRepository.findById(runId);
            if (modelRunOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ModelRun modelRun = modelRunOpt.get();
            
            // Verify the model run belongs to the specified model
            if (!modelRun.getModel().getId().equals(id)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Model run does not belong to specified model"));
            }
            return ResponseEntity.ok(ApiResponse.success(modelRun));
            
        } catch (Exception e) {
            logger.error("Error fetching model run details for model id: {} and run id: {}", id, runId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch model run details: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/{runId}")
    public ResponseEntity<ApiResponse<String>> deleteModelRun(
            @PathVariable Long id,
            @PathVariable Long runId) {
        logger.info("Deleting model run for model id: {} and run id: {}", id, runId);
        try {
            Optional<ModelRun> modelRunOpt = modelRunRepository.findById(runId);
            if (!modelRunOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            ModelRun modelRun = modelRunOpt.get();
            
            // Verify the model run belongs to the specified model
            if (!modelRun.getModel().getId().equals(id)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Model run does not belong to specified model"));
            }

            // TODO: Implement cascading deletion logic
            // This should:
            // 1. Delete associated ModelRunParam entities
            // 2. Delete associated ModelRunMetric entities
            // 3. Cancel any running training processes
            // 4. Clean up any temporary files or resources
            
            modelRunRepository.delete(modelRun);
            
            logger.info("Deleted model run with id: {}", runId);
            return ResponseEntity.ok(ApiResponse.success("Model run deleted successfully"));
            
        } catch (Exception e) {
            logger.error("Error deleting model run for model id: {} and run id: {}", id, runId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete model run: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/train/{runId}/predict")
    public ResponseEntity<ApiResponse<Object>> predictWithModel(
            @PathVariable Long id,
            @PathVariable Long runId,
            @RequestParam Map<String, String> queryParams) {
        logger.info("Making prediction with model id: {}, run id: {} and params: {}", id, runId, queryParams);
        try {

            Optional<Model> modelOpt = modelRepository.findById(id);
            if (!modelOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            Model model = modelOpt.get();

            Optional<ModelRun> modelRunOpt = modelRunRepository.findById(runId);
            if (!modelRunOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            ModelRun modelRun = modelRunOpt.get();
            
            // Verify the model run belongs to the specified model
            if (!modelRun.getModel().getId().equals(id)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Model run does not belong to specified model"));
            }

            // Verify the model run is completed and has a trained pipeline
            if (!"SUCCESS".equals(modelRun.getRunStatus())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Model run is not completed. Current status: " + modelRun.getRunStatus()));
            }

            // TODO: Implement actual prediction logic
           String s= mlService.loadPredictions(model, modelRun, queryParams);
            System.err.println(s);
            // This should:
            // 1. Load the trained model pipeline from modelRun.getRunResult()
            // 2. Process the query parameters as input features
            // 3. Execute the prediction using the trained model
            // 4. Return formatted prediction results
            
            Object predictionResult = "Prediction result placeholder - implement actual inference logic";
            
            logger.info("Generated prediction for model run id: {}", runId);
            return ResponseEntity.ok(ApiResponse.success("Prediction completed", predictionResult));
            
        } catch (Exception e) {
            logger.error("Error making prediction with model id: {} and run id: {}", id, runId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to make prediction: " + e.getMessage()));
        }
    }
}