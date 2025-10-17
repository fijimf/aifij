package com.fijimf.deepfij.ml.generators.models;

import com.fijimf.deepfij.ml.ModelImpl;
import com.fijimf.deepfij.ml.generators.feature.DayOfSeasonGenerator;
import com.fijimf.deepfij.ml.generators.feature.StatisticGenerator;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.repo.TeamStatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class KitchenSink implements ModelImpl {
    @Autowired
    private final TeamStatisticRepository teamStatisticRepository;
    private final DayOfSeasonGenerator dayOfSeason;
    private final StatisticGenerator wins;
    private final StatisticGenerator losses;
    private final StatisticGenerator weekAgoWins;
    private final StatisticGenerator linear;
    private final StatisticGenerator logistic;
    private final StatisticGenerator rpi;
    private final StatisticGenerator normalizedPF;
    private final StatisticGenerator decayWp;

    public KitchenSink(@Autowired TeamStatisticRepository teamStatisticRepository) {
        this.teamStatisticRepository = teamStatisticRepository;
        this.dayOfSeason = new DayOfSeasonGenerator();
        wins = new StatisticGenerator("WINS", teamStatisticRepository);
        losses = new StatisticGenerator("LOSSES", teamStatisticRepository);
        weekAgoWins = new StatisticGenerator("WINS", teamStatisticRepository);
        linear = new StatisticGenerator("LINEAR_REGRESSION", teamStatisticRepository);
        logistic = new StatisticGenerator("LOGISTIC_REGRESSION", teamStatisticRepository);
        rpi = new StatisticGenerator("RPI", teamStatisticRepository);
        normalizedPF = new StatisticGenerator("NORM_PF_AVG", teamStatisticRepository);
        decayWp = new StatisticGenerator("DECAYING_WIN_PCT", teamStatisticRepository);

    }

    @Override
    public String name() {
        return "kitchen-sink";
    }

    @Override
    public String description() {
        return "Naive linear regression of scores using a one-hot encoding of the home and away teams";
    }

    @Override
    public String type() {
        return "MultiSeason";
    }

    @Override
    public List<String> featureNames() {
        return List.of("", "");
    }

    @Override
    public List<String> labelNames() {
        return List.of("margin");
    }

    @Override
    public Map<String, Object> features(Game game) {
        Map<String, Object> features = new HashMap<>();

        features.put("day_of_season", dayOfSeason.generateFeature(game));
        features.put("wins", wins.generateFeature(game, "home", "z", 0));
        features.put("losses", losses.generateFeature(game, "home", "z", 0));
        features.put("weekAgoWins", wins.generateFeature(game, "home", "z", 7));
        features.put("regression", linear.generateFeature(game, "home", "z", 0));
        features.put("logistic", logistic.generateFeature(game, "home", "z", 0));
        features.put("rpi", rpi.generateFeature(game, "home", "z", 0));
        features.put("normalizedPF", normalizedPF.generateFeature(game, "home", "z", 0));
        features.put("decayWP", decayWp.generateFeature(game, "home", "z", 0));
        return features;
    }

    @Override
    public Map<String, Object> labels(Game games) {
        if (games.getHomeScore() == null || games.getAwayScore() == null) {
            throw new IllegalArgumentException("Home and away scores must be set");
        }
        return Map.of("margin", games.getHomeScore() - games.getAwayScore());
    }
}
