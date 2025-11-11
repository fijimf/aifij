package com.fijimf.deepfij.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fijimf.deepfij.config.initialization.InitializationConfig;
import com.fijimf.deepfij.config.initialization.SeasonConfig;
import com.fijimf.deepfij.config.initialization.StatisticToLoadConfig;
import com.fijimf.deepfij.config.initialization.StatisticsConfig;
import com.fijimf.deepfij.repo.StatisticTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "deepfij.refresh.enabled", havingValue = "true", matchIfMissing = true)
public class ScheduledRefreshService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledRefreshService.class);

    private final ScheduleService scheduleService;
    private final StatisticalService statisticalService;
    private final StatisticTypeRepository statisticTypeRepository;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final String initializationConfigPath;

    public ScheduledRefreshService(
            ScheduleService scheduleService,
            StatisticalService statisticalService,
            StatisticTypeRepository statisticTypeRepository,
            ResourceLoader resourceLoader,
            ObjectMapper objectMapper,
            @Value("${deepfij.initialization:}") String initializationConfigPath) {
        this.scheduleService = scheduleService;
        this.statisticalService = statisticalService;
        this.statisticTypeRepository = statisticTypeRepository;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.initializationConfigPath = initializationConfigPath;
    }

    @Scheduled(cron = "${deepfij.refresh.cron}", zone = "America/New_York")
    @Transactional
    public void refreshCurrentSeasons() {
        logger.info("Starting scheduled refresh of current seasons");
        long startTime = System.currentTimeMillis();

        try {
            InitializationConfig config = loadConfiguration();

            if (config.schedule() == null || config.schedule().seasons() == null) {
                logger.info("No schedule configuration found, skipping refresh");
                return;
            }

            // Find all current seasons and refresh them
            List<SeasonConfig> currentSeasons = config.schedule().seasons().stream()
                    .filter(SeasonConfig::isCurrent)
                    .toList();

            if (currentSeasons.isEmpty()) {
                logger.info("No current seasons configured, skipping refresh");
                return;
            }

            Map<Integer, Integer> gamesModifiedByYear = new HashMap<>();
            int totalGamesModified = 0;

            // Refresh each current season
            for (SeasonConfig seasonConfig : currentSeasons) {
                logger.info("Refreshing current season {}", seasonConfig.year());
                int gamesModified = scheduleService.refresh(seasonConfig.year());
                gamesModifiedByYear.put(seasonConfig.year(), gamesModified);
                totalGamesModified += gamesModified;
                logger.info("Refreshed {} games for season {}", gamesModified, seasonConfig.year());
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Scheduled refresh completed successfully in {} ms. Games modified: {}",
                    duration, totalGamesModified);

        } catch (Exception e) {
            logger.error("Scheduled refresh failed", e);
        }
    }

    @Scheduled(cron = "${deepfij.refreshStats.cron}", zone = "America/New_York")
    @Transactional
    public void updateStatistics() {

        InitializationConfig config = loadConfiguration();

        if (config.schedule() == null || config.schedule().seasons() == null || config.statistics() == null) {
            logger.info("Schedule configuration or statistic config missing, skipping refresh");
            return;
        }

        // Find all current seasons and refresh them
        List<SeasonConfig> currentSeasons = config.schedule().seasons().stream()
                .filter(SeasonConfig::isCurrent)
                .toList();


        currentSeasons.forEach(seasonConfig -> {
            StatisticsConfig statisticsConfig = config.statistics();

            for (StatisticToLoadConfig statConfig : statisticsConfig.statisticsToLoad()) {
                if (statisticTypeRepository.findByModelKey(statConfig.key()).isEmpty()) {
                    logger.warn("Statistic key '{}' not found in database, skipping", statConfig.key());
                    continue;
                }
                logger.info("Updating statistics '{}' for season {}", statConfig.key(), seasonConfig.year());
                try {
                    statisticalService.generateStatistics(seasonConfig.year().toString(), statConfig.key());
                } catch (Exception e) {
                    logger.error("Failed to update statistics '{}' for season {}: {}",
                            statConfig.key(), seasonConfig.year(), e.getMessage());
                }
            }
        });
    }

    private InitializationConfig loadConfiguration() {
        if (initializationConfigPath == null || initializationConfigPath.trim().isEmpty()) {
            logger.debug("No initialization configuration specified, using defaults");
            return new InitializationConfig();
        }

        try {
            String configPath = initializationConfigPath.startsWith("classpath:") ?
                    initializationConfigPath : "file:" + initializationConfigPath;

            logger.debug("Loading initialization configuration from: {}", configPath);

            try (InputStream inputStream = resourceLoader.getResource(configPath).getInputStream()) {
                InitializationConfig config = objectMapper.readValue(inputStream, InitializationConfig.class);
                logger.debug("Configuration loaded successfully");
                return config;
            }
        } catch (Exception e) {
            logger.warn("Failed to load initialization configuration from {}, using defaults: {}",
                    initializationConfigPath, e.getMessage());
            return new InitializationConfig();
        }
    }
}
