package com.fijimf.deepfij.service.stat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repo.TeamRepository;
import com.fijimf.deepfij.service.StatisticTypeService;
import com.fijimf.deepfij.service.StatisticalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogisticRegressionStatisticModel implements StatisticalModel {
    private static final Logger logger = LoggerFactory.getLogger(LogisticRegressionStatisticModel.class);
    private final RestTemplate restTemplate;
    private final TeamRepository teamRepository;
    private final StatisticTypeService statisticTypeService;
    private final String apiUrl;

    @Autowired
    public LogisticRegressionStatisticModel(TeamRepository teamRepository, StatisticTypeService statisticTypeService, @Value("${pystats.api.url:http://127.0.0.1:5000}") String apiUrl) {
        this.teamRepository = teamRepository;
        this.statisticTypeService = statisticTypeService;
        this.restTemplate = new RestTemplate();
        this.apiUrl = apiUrl;
    }

    @PostConstruct
    public void checkBackendHealth() {
        try {
            String healthUrl = apiUrl + "/api/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            
            if (response.getStatusCode() != HttpStatus.OK) {
                logger.warn("Backend model server health check failed with status: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            logger.warn("Failed to connect to backend model server at " + apiUrl + "/api/health");
        }
    }

    @Override
    public String key() {
        return "LOGISTIC_REGRESSION";
    }

    @Override
    public List<StatisticType> refreshDBTypes() {
         return List.of(statisticTypeService.findOrCreateStatisticType("LOGISTIC_REGRESSION", "LOGISTIC_REGRESSION", "Logistic Regression", true, 4, key()));
    }

    @Override
    public List<TeamStatistic> generate(Season season) {
        StatisticType type = statisticTypeService.findStatisticType("LOGISTIC_REGRESSION");
        String url = String.format(apiUrl +"/api/rankings/logistic?year=%d", season.getYear());
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
