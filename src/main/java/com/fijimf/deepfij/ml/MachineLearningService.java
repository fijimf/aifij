package com.fijimf.deepfij.ml;

import com.fijimf.deepfij.model.ml.Model;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.repo.GameRepository;
import com.fijimf.deepfij.repo.ModelRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MachineLearningService {
private static final Logger logger = org.slf4j.LoggerFactory.getLogger(MachineLearningService.class);
    private final Map<String, ModelImpl> models = new HashMap<>();

    private final GameRepository gameRepository;
    private final ModelRepository modelRepository;

    private final ApplicationContext applicationContext;

    public MachineLearningService(GameRepository gameRepository, ModelRepository modelRepository, ApplicationContext applicationContext) {
        this.gameRepository = gameRepository;
        this.modelRepository = modelRepository;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void initializeModels() {
        logger.info("Initializing ML models by scanning ApplicationContext for ModelImpl beans");
        
        // Get all ModelImpl beans from the application context
        Map<String, ModelImpl> modelImplBeans = applicationContext.getBeansOfType(ModelImpl.class);
        logger.info("Found {} ModelImpl beans in ApplicationContext", modelImplBeans.size());
        
        // Update the models map
        models.clear();
        models.putAll(modelImplBeans);
        
        // Get all existing models from database
        List<Model> existingModels = modelRepository.findAll();
        Set<String> beanClassNames = modelImplBeans.values().stream()
                .map(bean -> bean.getClass().getName())
                .collect(Collectors.toSet());
        
        // Update or create models based on beans
        for (ModelImpl modelBean : modelImplBeans.values()) {
            String className = modelBean.getClass().getName();
            Model existingModel = existingModels.stream()
                    .filter(m -> className.equals(m.getClassName()))
                    .findFirst()
                    .orElse(null);
                    
            if (existingModel != null) {
                // Update existing model
                existingModel.setName(modelBean.name());
                existingModel.setType(modelBean.type());
                existingModel.setDescription(modelBean.description());
                existingModel.setClassName(className);
                existingModel.setFeaturesOk(true);
                modelRepository.save(existingModel);
                logger.info("Updated existing model: {}", modelBean.name());
            } else {
                // Create new model
                Model newModel = new Model(
                    modelBean.name(),
                    modelBean.type(),
                    modelBean.description(),
                    className,
                    true, // featuresOk = true since bean exists
                    false // pipelineOk = false initially
                );
                modelRepository.save(newModel);
                logger.info("Created new model: {}", modelBean.name());
            }
        }
        
        // Set featuresOk to false for database models without corresponding beans
        for (Model model : existingModels) {
            if (!beanClassNames.contains(model.getClassName())) {
                model.setFeaturesOk(false);
                modelRepository.save(model);
                logger.warn("Model {} has no corresponding bean - set featuresOk to false", model.getName());
            }
        }
        
        logger.info("Model initialization complete");
    }

}
