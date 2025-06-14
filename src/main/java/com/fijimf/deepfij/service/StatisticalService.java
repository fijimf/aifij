package com.fijimf.deepfij.service;

import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repo.SeasonRepository;
import com.fijimf.deepfij.repo.TeamStatisticRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

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
}
