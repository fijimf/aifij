package com.fijimf.deepfij.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repo.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogisticRegressionStatisticModel implements StatisticalModel {
    private final RestTemplate restTemplate;
    private final TeamRepository teamRepository;
    private final StatisticTypeService statisticTypeService;

    @Autowired
    public LogisticRegressionStatisticModel(TeamRepository teamRepository, StatisticTypeService statisticTypeService) {
        this.teamRepository = teamRepository;
        this.statisticTypeService = statisticTypeService;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String key() {
        return "LOGISTIC_REGRESSION";
    }

    @Override
    public List<TeamStatistic> generate(Season season) {
        StatisticType type = statisticTypeService.findOrCreateStatisticType("LOGISTIC_REGRESSION", "LOGISTIC_REGRESSION", "Logistic Regression", true);
        String url = String.format("http://127.0.0.1:5000/api/rankings/logistic?year=%d", season.getYear());
        JsonNode response = restTemplate.getForObject(url, JsonNode.class);
        List<TeamStatistic> statistics = new ArrayList<>();
        if (response != null && response.has("data")) {
            JsonNode data = response.get("data");
            data.fields().forEachRemaining(entry -> {
                LocalDate date = LocalDate.parse(entry.getKey());
                JsonNode dateData = entry.getValue();

                dateData.fields().forEachRemaining(teamEntry -> {
                    Team t = teamRepository.findByAbbreviation(teamEntry.getKey());
                    statistics.add(new TeamStatistic.TeamStatisticBuilder()
                            .withSeason(season)
                            .withDate(date)
                            .withType(type)
                            .withTeam(t)
                            .withValue(BigDecimal.valueOf(teamEntry.getValue().asDouble())).build()
                    );
                });
            });
        }
        return statistics;
    }
}
