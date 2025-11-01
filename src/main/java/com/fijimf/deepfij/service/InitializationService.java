package com.fijimf.deepfij.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fijimf.deepfij.config.initialization.*;
import com.fijimf.deepfij.ml.MachineLearningService;
import com.fijimf.deepfij.model.ml.ModelRun;
import com.fijimf.deepfij.repo.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class InitializationService {

    private static final Logger logger = LoggerFactory.getLogger(InitializationService.class);

    private final ScheduleService scheduleService;
    private final MachineLearningService machineLearningService;
    private final TeamRepository teamRepository;
    private final ConferenceRepository conferenceRepository;
    private final GameRepository gameRepository;
    private final SeasonRepository seasonRepository;
    private final StatisticTypeRepository statisticTypeRepository;
    private final TeamStatisticRepository teamStatisticRepository;
    private final ModelRepository modelRepository;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final String initializationConfigPath;
    private final StatisticalService statisticalService;

    public InitializationService(
            ScheduleService scheduleService,
            MachineLearningService machineLearningService,
            TeamRepository teamRepository,
            ConferenceRepository conferenceRepository,
            GameRepository gameRepository,
            SeasonRepository seasonRepository,
            StatisticTypeRepository statisticTypeRepository, TeamStatisticRepository teamStatisticRepository,
            ModelRepository modelRepository,
            ResourceLoader resourceLoader,
            ObjectMapper objectMapper,
            Validator validator,
            @Value("${deepfij.initialization:}") String initializationConfigPath, StatisticalService statisticalService) {
        this.scheduleService = scheduleService;
        this.machineLearningService = machineLearningService;
        this.teamRepository = teamRepository;
        this.conferenceRepository = conferenceRepository;
        this.gameRepository = gameRepository;
        this.seasonRepository = seasonRepository;
        this.statisticTypeRepository = statisticTypeRepository;
        this.teamStatisticRepository = teamStatisticRepository;
        this.modelRepository = modelRepository;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.initializationConfigPath = initializationConfigPath;
        this.statisticalService = statisticalService;
    }

    @Transactional
    public void performInitialization() {
        logger.info("Starting database initialization");
        long startTime = System.currentTimeMillis();

        try {
            InitializationConfig config = loadConfiguration();
            validateConfiguration(config);

            InitializationState state = new InitializationState();

            // Execute initialization phases sequentially
            initializeScheduleData(config.schedule(), state);
            initializeStatistics(config.statistics(), state);
            initializeModels(config.models(), state);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Database initialization completed successfully in {} ms", duration);

        } catch (Exception e) {
            logger.error("Database initialization failed", e);
            throw new RuntimeException("Initialization failed", e);
        }
    }

    private InitializationConfig loadConfiguration() {
        if (initializationConfigPath == null || initializationConfigPath.trim().isEmpty()) {
            logger.info("No initialization configuration specified, using defaults");
            return new InitializationConfig();
        }

        try {
            String configPath = initializationConfigPath.startsWith("classpath:") ?
                    initializationConfigPath : "file:" + initializationConfigPath;

            logger.info("Loading initialization configuration from: {}", configPath);

            try (InputStream inputStream = resourceLoader.getResource(configPath).getInputStream()) {
                InitializationConfig config = objectMapper.readValue(inputStream, InitializationConfig.class);
                logger.info("Configuration loaded successfully");
                return config;
            }
        } catch (Exception e) {
            logger.warn("Failed to load initialization configuration from {}, using defaults: {}",
                    initializationConfigPath, e.getMessage());
            return new InitializationConfig();
        }
    }

    private void validateConfiguration(InitializationConfig config) {
        Set<ConstraintViolation<InitializationConfig>> violations = validator.validate(config);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Configuration validation failed:");
            for (ConstraintViolation<InitializationConfig> violation : violations) {
                sb.append("\n- ").append(violation.getPropertyPath()).append(": ").append(violation.getMessage());
            }
            throw new IllegalArgumentException(sb.toString());
        }

        // Additional business logic validation
        validateBusinessRules(config);
    }

    private void validateBusinessRules(InitializationConfig config) {
        if (config.schedule() != null && config.schedule().seasons() != null) {
            // Check that only one season is marked as current
            long currentSeasonCount = config.schedule().seasons().stream()
                    .mapToLong(s -> s.isCurrent() ? 1 : 0)
                    .sum();

            if (currentSeasonCount > 1) {
                throw new IllegalArgumentException("Only one season can be marked as current");
            }
        }
    }

    private void initializeScheduleData(ScheduleConfig scheduleConfig, InitializationState state) {
        if (scheduleConfig == null) {
            logger.info("No schedule configuration provided, skipping schedule initialization");
            return;
        }

        logger.info("Starting schedule data initialization");

        // Initialize teams
        if (shouldLoadTeams(scheduleConfig)) {
            logger.info("Loading teams from ESPN");
            scheduleService.loadTeams();
            state.teamsLoaded = true;
        }

        // Initialize conferences
        if (shouldLoadConferences(scheduleConfig, state)) {
            logger.info("Loading conferences from ESPN");
            scheduleService.loadConferences();
            state.conferencesLoaded = true;
        }

        // Initialize seasons and games
        for (SeasonConfig seasonConfig : scheduleConfig.seasons()) {
            if (shouldLoadGamesForSeason(seasonConfig, state)) {
                logger.info("Loading games for season {}", seasonConfig.year());
                boolean created = scheduleService.createSchedule(seasonConfig.year());
                if (created) {
                    state.seasonsLoaded.put(seasonConfig.year(), true);
                }
            }

            // Handle current season updates
            if (seasonConfig.isCurrent()) {
                logger.info("Refreshing current season {} with recent games", seasonConfig.year());
                scheduleService.refresh(seasonConfig.year());
            }
        }

        logger.info("Schedule data initialization completed");
    }

    private boolean shouldLoadTeams(ScheduleConfig config) {
        return switch (config.loadTeams()) {
            case "always" -> true;
            case "check" -> teamRepository.count() < config.minTeams();
            case "never" -> false;
            default -> false;
        };
    }

    private boolean shouldLoadConferences(ScheduleConfig config, InitializationState state) {
        return switch (config.loadConferences()) {
            case "always" -> true;
            case "check" -> conferenceRepository.count() < config.minConferences() || state.teamsLoaded;
            case "never" -> false;
            default -> false;
        };
    }

    private boolean shouldLoadGamesForSeason(SeasonConfig seasonConfig, InitializationState state) {
        return switch (seasonConfig.loadGames()) {
            case "always" -> true;
            case "check" -> {
                var seasons = seasonRepository.findByYear(seasonConfig.year());
                if (seasons.isEmpty()) {
                    yield true; // Season doesn't exist, need to load
                }
                var season = seasons.get(0);
                long gameCount = gameRepository.countBySeasonEquals(season);
                yield gameCount == 0 || state.teamsLoaded || state.conferencesLoaded;
            }
            case "never" -> false;
            default -> false;
        };
    }

    private void initializeStatistics(StatisticsConfig statisticsConfig, InitializationState state) {
        if (statisticsConfig == null || statisticsConfig.statisticsToLoad().isEmpty()) {
            logger.info("No statistics configuration provided, skipping statistics initialization");
            return;
        }

        logger.info("Starting statistics initialization");

        for (StatisticToLoadConfig statConfig : statisticsConfig.statisticsToLoad()) {
            // Validate statistic key exists
            if (statisticTypeRepository.findByModelKey(statConfig.key()).isEmpty()) {
                logger.warn("Statistic key '{}' not found in database, skipping", statConfig.key());
                continue;
            }
            Map<Integer, Map<String, Integer>> countStatsBySeasonAndModel = countStatsBySeasonAndModel();
            for (Integer year : statConfig.seasons()) {
                Integer count = countStatsBySeasonAndModel.getOrDefault(year, Map.of()).getOrDefault(statConfig.key(), 0);
                if (shouldLoadStatisticsForSeason(statConfig.key(), year, state, count)) {
                    logger.info("Loading statistics '{}' for season {}", statConfig.key(), year);
                    try {
//                        statisticService.calculateStatisticsForSeason(year, statConfig.key());
                        statisticalService.generateStatistics(year.toString(), statConfig.key());
                    } catch (Exception e) {
                        logger.error("Failed to load statistics '{}' for season {}: {}",
                                statConfig.key(), year, e.getMessage());
                    }
                }
            }
        }

        logger.info("Statistics initialization completed");
    }

    private Map<Integer, Map<String, Integer>> countStatsBySeasonAndModel() {
        Map<Integer, Map<String, Integer>> resp = new HashMap<>();
        teamStatisticRepository.countTeamsBySeasonAndModel()
                .forEach(map -> {
                    int year = Integer.parseInt(map.get("year").toString().replaceAll("[^0-9]", ""));
                    String key = map.get("model_key").toString();
                    int count = Integer.parseInt(map.get("count").toString());
                    resp.putIfAbsent(year, new HashMap<>());
                    resp.get(year).put(key, count);
                });
        return resp;
    }

    private boolean shouldLoadStatisticsForSeason(String statisticKey, Integer year, InitializationState state, int count) {
        // Always reload if games were reloaded for this season
        if (state.seasonsLoaded.getOrDefault(year, false)) {
            return true;
        }

        // Check if statistics already exist for this combination
        var seasons = seasonRepository.findByYear(year);
        if (seasons.isEmpty()) {
            return false; // Season doesn't exist
        }

        var statisticTypes = statisticTypeRepository.findByModelKey(statisticKey);
        if (statisticTypes.isEmpty()) {
            return false; // Statistic type doesn't exist
        }
        return count == 0;
    }

    private void initializeModels(ModelsConfig modelsConfig, InitializationState state) {
        if (modelsConfig == null || modelsConfig.modelsToTrain().isEmpty()) {
            logger.info("No models configuration provided, skipping model training");
            return;
        }

        logger.info("Starting model training");

        for (ModelToTrainConfig modelConfig : modelsConfig.modelsToTrain()) {
            try {
                logger.info("Training model '{}'", modelConfig.name());

                // Find the model in the database
                var models = modelRepository.findAllByName(modelConfig.name());
                if (models.isEmpty()) {
                    logger.warn("Model '{}' not found in database, skipping", modelConfig.name());
                    continue;
                }

                var model = models.get(0);

                // Convert parameters map to the format expected by ML service
                Map<String, String> stringParams = new HashMap<>();
                modelConfig.parameters().forEach((key, value) ->
                        stringParams.put(key, value.toString()));

                // First, create and save the ModelRun in current transaction
                ModelRun modelRun = machineLearningService.prepareModelRun(model.getId(), new HashMap<>(stringParams));
                
                // Then start training in separate transaction so ModelRun is committed before ML server call
                startTrainingInSeparateTransaction(model.getId(), modelRun.getId(), stringParams);
                logger.info("Model '{}' training completed", modelConfig.name());

            } catch (Exception e) {
                logger.error("Failed to train model '{}': {}", modelConfig.name(), e.getMessage());
            }
        }

        logger.info("Model training completed");
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startTrainingInSeparateTransaction(Long modelId, Long modelRunId, Map<String, String> stringParams) {
        machineLearningService.startModelTraining(modelId, modelRunId, stringParams);
    }

    private static class InitializationState {
        boolean teamsLoaded = false;
        boolean conferencesLoaded = false;
        Map<Integer, Boolean> seasonsLoaded = new HashMap<>();
    }
}