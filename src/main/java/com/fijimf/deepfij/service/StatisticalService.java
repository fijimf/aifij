package com.fijimf.deepfij.service;

import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repo.SeasonRepository;
import com.fijimf.deepfij.repo.TeamStatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StatisticalService {
    private final TeamStatisticRepository teamStatisticRepository;
    private final SeasonRepository seasonRepository;
    private final WonLostStatisticModel wonLostStatisticModel;
    private final PointsStatisticModel pointsStatisticModel;
    private final LinearRegressionStatisticModel linearRegressionStatisticModel;
    private final Map<String, StatisticalModel> statisticalModels;
    private final LogisticRegressionStatisticModel logisticRegressionStatisticModel;

    @Autowired
    public StatisticalService(@Autowired TeamStatisticRepository teamStatisticRepository, @Autowired SeasonRepository seasonRepository, @Autowired WonLostStatisticModel wonLostStatisticModel, @Autowired PointsStatisticModel pointsStatisticModel, LinearRegressionStatisticModel linearRegressionStatisticModel, LogisticRegressionStatisticModel logisticRegressionStatisticModel) {
        this.teamStatisticRepository = teamStatisticRepository;
        this.seasonRepository = seasonRepository;
        this.wonLostStatisticModel = wonLostStatisticModel;
        this.pointsStatisticModel = pointsStatisticModel;
        this.linearRegressionStatisticModel = linearRegressionStatisticModel;
        statisticalModels = Map.of(
                wonLostStatisticModel.key(), wonLostStatisticModel,
                pointsStatisticModel.key(), pointsStatisticModel,
                linearRegressionStatisticModel.key(), linearRegressionStatisticModel,
                logisticRegressionStatisticModel.key(), logisticRegressionStatisticModel);
        this.logisticRegressionStatisticModel = logisticRegressionStatisticModel;
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




}
