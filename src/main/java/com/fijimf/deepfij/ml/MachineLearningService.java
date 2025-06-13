package com.fijimf.deepfij.ml;

import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.repo.GameRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MachineLearningService {
private static final Logger logger = org.slf4j.LoggerFactory.getLogger(MachineLearningService.class);
    private final Map<String, ModelGenerator> models = new HashMap<>();

    private final GameRepository gameRepository;
    @Autowired
    private ApplicationContext applicationContext;

    public MachineLearningService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @PostConstruct
    public void initializeModels() {
        Map<String, FeatureGenerator> featureGenerators = applicationContext.getBeansOfType(FeatureGenerator.class);
        Map<String, TargetGenerator> targetGenerators = applicationContext.getBeansOfType(TargetGenerator.class);

        logger.info("Found {} feature generators and {} target generators", featureGenerators.size(), targetGenerators.size());
        for (Map.Entry<String, FeatureGenerator> fg : featureGenerators.entrySet()) {
            fg.getValue().getModelsSupported().forEach(featureModel -> {
                for (Map.Entry<String, TargetGenerator> tg : targetGenerators.entrySet()) {
                    tg.getValue().getModelsSupported().forEach(targetModel -> {
                        if (featureModel.equals(targetModel)) {
                            logger.info("Registering model: {} for feature model: {} and target model: {}",
                                    targetModel, fg.getValue().getKey(),  tg.getValue().getKey());
                            registerModel(targetModel, fg.getValue(), tg.getValue());
                        }

                    });

                }
            });
        }
    }

    public List<String> getKeys() {
        return new ArrayList<>(models.keySet());
    }

    public void registerModel(String key, FeatureGenerator featureGenerator, TargetGenerator targetGenerator) {
        models.put(key, new ModelGenerator(featureGenerator, targetGenerator));
    }

    public ModelData getModelData(String key, Integer year) {
        ModelGenerator modelGenerator = models.get(key);
        if (modelGenerator != null) {
            FeatureGenerator featureGenerator = modelGenerator.featureGenerator();
            TargetGenerator targetGenerator = modelGenerator.targetGenerator();
            List<Map<String, Object>> features = new ArrayList<>();
            List<Map<String, Object>> targets = new ArrayList<>();

            List<Game> games = year == null ? gameRepository.findAll() : gameRepository.findBySeasonYear(year);

            games.forEach(game -> {
                Map<String, Object> feature = featureGenerator.generateFeature(game);
                Map<String, Object> target = targetGenerator.generateTarget(game);
                features.add(feature);
                targets.add(target);
                if (features.size() % 1000 == 0) {
                    logger.info("Generated {} features and {} targets", features.size(), targets.size());
                }
            });
            return new ModelData(key, key, key, LocalDateTime.now(), features, targets);
        } else {
            throw new IllegalArgumentException("No model found for key: " + key);
        }
    }

    public record ModelGenerator(FeatureGenerator featureGenerator, TargetGenerator targetGenerator) {
    }
}
