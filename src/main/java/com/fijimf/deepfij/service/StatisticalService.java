package com.fijimf.deepfij.service;

import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repo.SeasonRepository;
import com.fijimf.deepfij.repo.TeamStatisticRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticalService {
    private final TeamStatisticRepository teamStatisticRepository;
    private final SeasonRepository seasonRepository;
    private final ApplicationContext applicationContext;
    private Map<String, StatisticalModel> statisticalModels;

    @Autowired
    public StatisticalService(@Autowired ApplicationContext applicationContext, @Autowired TeamStatisticRepository teamStatisticRepository, @Autowired SeasonRepository seasonRepository, @Autowired WonLostStatisticModel wonLostStatisticModel, @Autowired PointsStatisticModel pointsStatisticModel, LinearRegressionStatisticModel linearRegressionStatisticModel, LogisticRegressionStatisticModel logisticRegressionStatisticModel) {
        this.teamStatisticRepository = teamStatisticRepository;
        this.seasonRepository = seasonRepository;
        this.applicationContext = applicationContext;
        statisticalModels = new HashMap<>();
    }

    @PostConstruct
    public void initializeModels() {
        Map<String, StatisticalModel> models = applicationContext.getBeansOfType(StatisticalModel.class);
        for (Map.Entry<String, StatisticalModel> fg : models.entrySet()) {
            fg.getValue().refreshDBTypes();
            statisticalModels.put(fg.getValue().key(), fg.getValue());
        }
    }


    public List<TeamStatistic> generateStatistics(String yyyy, String modelKey) {
        Season season = seasonRepository.findByYear(Integer.parseInt(yyyy)).getFirst();
        if (season == null) {
            throw new IllegalArgumentException("No season found for year: " + yyyy);
        }
        StatisticalModel model = statisticalModels.get(modelKey);
        if (model == null) {
            throw new IllegalArgumentException("Invalid model: " + modelKey);
        }

        List<TeamStatistic> statistics = model.generate(season);
        return teamStatisticRepository.saveAll(statistics);
    }


    public List<String> modelKeys() {
        return statisticalModels.keySet().stream().sorted().toList();
    }

    public StatisticalService.StatisticsStatus getStatisticStatus() {
        List<Map<String, Object>> summary = teamStatisticRepository.findSummary();
        Map<String, Map<String, Map<String, List<TeamStatSeasonStatus>>>> modelMap = buildModelHierarchy(summary);
        statisticalModels.forEach((k, v) -> {
            if (!modelMap.containsKey(k)) {
                modelMap.put(k, new HashMap<>());
                v.refreshDBTypes().forEach(type -> {
                    modelMap.get(k).put(v.key(), new HashMap<>());
                });
            }
        });

        return createStatisticsStatus(modelMap);
    }

    private Map<String, Map<String, Map<String, List<TeamStatSeasonStatus>>>> buildModelHierarchy(List<Map<String, Object>> summary) {
        Map<String, Map<String, Map<String, List<TeamStatSeasonStatus>>>> model = new HashMap<>();

        summary.forEach(r -> {
            String modelKey = (String) r.get("model");
            String statKey = (String) r.get("stat");
            String seasonKey = Integer.toString((int) r.get("season"));

            TeamStatSeasonStatus status = new TeamStatSeasonStatus(
                    Integer.parseInt(seasonKey),
                    ((Long) r.get("num_days")).intValue(),
                    ((BigDecimal) r.get("total")).intValue(),
                    ( (Date) r.get("last_date")).toLocalDate()
            );

            model.computeIfAbsent(modelKey, k -> new HashMap<>())
                    .computeIfAbsent(statKey, k -> new HashMap<>())
                    .computeIfAbsent(seasonKey, k -> new ArrayList<>())
                    .add(status);
        });

        return model;
    }

    private StatisticsStatus createStatisticsStatus(Map<String, Map<String, Map<String, List<TeamStatSeasonStatus>>>> models) {
        return StatisticsStatus.fromMap(models);
    }

    public record StatisticsStatus(
            List<ModelStatus> models
    ) {
        public static StatisticsStatus fromMap(Map<String, Map<String, Map<String, List<TeamStatSeasonStatus>>>> models) {
            return new StatisticsStatus(models.entrySet().stream().map(ModelStatus::fromEntry).toList());
        }
    }

    public record ModelStatus(
            String key,
            List<TeamStatisticStatus> teamStats
    ) {


        public static ModelStatus fromEntry(Map.Entry<String, Map<String, Map<String, List<TeamStatSeasonStatus>>>> entry) {
            return new ModelStatus(entry.getKey(), entry.getValue().entrySet().stream().map(TeamStatisticStatus::fromEntry).toList());
        }
    }

    public record TeamStatisticStatus(
            String key,
            List<TeamStatSeasonStatus> seasons
    ) {
        public static TeamStatisticStatus fromEntry(Map.Entry<String, Map<String, List<TeamStatSeasonStatus>>> entry) {
            return new TeamStatisticStatus(
                    entry.getKey(),
                    entry.getValue().entrySet().stream().flatMap(e -> e.getValue().stream()).toList()
            );
        }
    }

    public record TeamStatSeasonStatus(
            int year,
            int numDates,
            int numStats,
            LocalDate lastDate
    ) {
    }
}
