package com.fijimf.deepfij.ml;

import com.fijimf.deepfij.model.ml.Model;
import com.fijimf.deepfij.model.ml.ModelRun;
import com.fijimf.deepfij.model.ml.ModelRunMetric;
import com.fijimf.deepfij.model.ml.ModelRunParam;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.repo.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class MachineLearningService {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(MachineLearningService.class);
    private final Map<String, ModelImpl> models = new HashMap<>();

    private final GameRepository gameRepository;
    private final ModelRepository modelRepository;
    private final ModelRunRepository modelRunRepository;

    private final ApplicationContext applicationContext;
    private final SeasonRepository seasonRepository;

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final ModelRunMetricRepository modelRunMetricRepository;
    private final ModelRunParamRepository modelRunParamRepository;

    public MachineLearningService(GameRepository gameRepository, ModelRepository modelRepository, ModelRunRepository modelRunRepository, ApplicationContext applicationContext, SeasonRepository seasonRepository, RestTemplate restTemplate, @Value("${pystats.api.url:http://127.0.0.1:5000}/api") String apiUrl, ModelRunMetricRepository modelRunMetricRepository, ModelRunParamRepository modelRunParamRepository) {
        this.gameRepository = gameRepository;
        this.modelRepository = modelRepository;
        this.modelRunRepository = modelRunRepository;
        this.applicationContext = applicationContext;
        this.seasonRepository = seasonRepository;
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.modelRunMetricRepository = modelRunMetricRepository;
        this.modelRunParamRepository = modelRunParamRepository;
    }

    @PostConstruct
    public void initializeModels() {
        logger.info("Initializing ML models by scanning ApplicationContext for ModelImpl beans");

        // Get all ModelImpl beans from the application context
        Map<String, ModelImpl> modelImplBeans = applicationContext.getBeansOfType(ModelImpl.class);
        logger.info("Found {} ModelImpl beans in ApplicationContext", modelImplBeans.size());

        // Update the models map
        models.clear();
        modelImplBeans.values().forEach(model -> models.put(model.name(), model));

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ModelRun prepareModelRun(Long modelId, Map<String, String> queryParams) {
        Optional<Model> modelOpt = modelRepository.findById(modelId);
        if (modelOpt.isEmpty()) throw new IllegalArgumentException("Invalid model id: " + modelId);

        Model model = modelOpt.get();
        String seasons = queryParams.get("seasons");
        if (seasons == null) throw new IllegalArgumentException("seasons required for training data");
        
        ModelRun modelRun = modelRunRepository.save(new ModelRun(model, LocalDateTime.now(), "STARTED"));
        queryParams.entrySet().stream().forEach(entry -> {
            modelRunParamRepository.save(new ModelRunParam(modelRun, entry.getKey(), entry.getValue()));
        });
        logger.info("Created model run with id: {} for model: {}", modelRun.getId(), model.getName());
        return modelRun;
    }
    
    public void startModelTraining(Long modelId, Long modelRunId, Map<String, String> queryParams) {
        Optional<Model> modelOpt = modelRepository.findById(modelId);
        if (modelOpt.isEmpty()) throw new IllegalArgumentException("Invalid model id: " + modelId);
        
        Model model = modelOpt.get();
        String seasons = queryParams.remove("seasons");
        if (seasons == null) throw new IllegalArgumentException("seasons required for training data");
        List<Integer> years = Arrays.stream(seasons.split(",")).map(Integer::parseInt).toList();
        List<Game> games = new ArrayList<>();

        if (model.getType().toUpperCase().contains("SINGLESEASON")) {
            List<Game> seasonGames = gameRepository.findBySeasonYear(years.getFirst());
            games.addAll(seasonGames);
        } else {
            for (Integer year : years) {
                List<Game> seasonGames = gameRepository.findBySeasonYear(year);
                games.addAll(seasonGames);
            }
        }
        Map<String, Object> parameters = new HashMap<>(queryParams);
        List<Map<String, Object>> features = new ArrayList<>();
        List<Map<String, Object>> labels = new ArrayList<>();

        games.stream().filter(Game::isComplete).forEach(g -> {
            features.add(models.get(model.getName()).features(g));
            labels.add(models.get(model.getName()).labels(g));
        });
        
        logger.info("Starting training for model: {}", model.getName());
        Map<String, Object> body = Map.of("parameters", parameters, "features", features, "labels", labels);
        String url = apiUrl + "/ml/train?model_name=" + model.getName() + "&model_run_id=" + modelRunId;
        logger.info("URL = "+url);
        ResponseEntity<String> resp = restTemplate.postForEntity(url, body, String.class);
        logger.info("Training request sent for model: {}", model.getName());
    }

    public String loadPredictions(Model model, ModelRun modelRun, Map<String, String> queryParams) {
        List<Game> games = loadGames(queryParams);
        List<Map<String, Object>> features = games.stream().map(models.get(model.getName())::features).toList();
        Map<String, Object> body = Map.of("features", features);
        String url = apiUrl + "/ml/predict?model_name=" + model.getName() + "&model_run_id=" + modelRun.getId();
        logger.info("URL = "+url);
        ResponseEntity<String> resp = restTemplate.postForEntity(url, body, String.class);
        logger.info("Response = "+resp);
        logger.info("Loaded predictions for model: {}", model.getName());
        return resp.getBody();
    }

    private List<Game> loadGames(Map<String, String> queryParams) {
        if (queryParams.containsKey("seasons")) {
             List<Game> games =Arrays.stream(queryParams.get("seasons").split(","))
                     .map(Integer::parseInt)
                     .flatMap(year ->
                        seasonRepository.findByYear(year).stream()
                                .flatMap(season -> season.getGames().stream())
             ).toList();
             return applyFilters(queryParams, games);
        } else if (queryParams.containsKey("dates")) {
            List<Game> games =Arrays.stream(queryParams.get("dates")
                    .split(","))
                    .map(s-> LocalDate.parse(s,  DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .map(MachineLearningService::dateToSeasonYear)
                    .flatMap(year ->
                            seasonRepository.findByYear(year).stream()
                                    .flatMap(season -> season.getGames().stream())
                    ).toList();
            return applyFilters(queryParams, games);

        } else if (queryParams.containsKey("gameIds")){
            return gameRepository.findAllById(Arrays.stream(queryParams.get("gameIds").split(",")).map(Long::parseLong).toList());
        } else {
            throw new IllegalArgumentException("Must specify seasons, dates or gameIds");
        }
    }

    private List<Game> applyFilters(Map<String, String> queryParams, List<Game> games) {
        List<Predicate<Game>> filters = new ArrayList<>();

        if (queryParams.containsKey("dates")) {
            Set<LocalDate> dates = Arrays.stream(queryParams.get("dates")
                            .split(","))
                    .map(s -> LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyyMMdd"))).collect(Collectors.toSet());
            filters.add(g -> dates.contains(g.getDate()));
        }
        if (queryParams.containsKey("team")) {
            String team = queryParams.get("team");
            filters.add(g -> g.getHomeTeam().getName().equals(team) || g.getAwayTeam().getName().equals(team));
        }
        if (queryParams.containsKey("skipPlayed")) {
            filters.add(Game::isComplete);
        }
        if (filters.isEmpty()) {
            return games;
        } else {
            return games.stream().filter(g -> filters.stream().allMatch(f -> f.test(g))).collect(Collectors.toList());
        }
    }

    private static int dateToSeasonYear(LocalDate date) {
        if (date.getMonthValue()<5) return date.getYear();
        else if (date.getMonthValue()>10) return date.getYear()+1;
        else throw new IllegalArgumentException("Non seasonal date" + date);
    }
}
