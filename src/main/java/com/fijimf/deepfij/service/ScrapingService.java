package com.fijimf.deepfij.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fijimf.deepfij.model.scraping.conference.ConferenceResponse;
import com.fijimf.deepfij.model.scraping.conference.RawConference;
import com.fijimf.deepfij.model.scraping.scoreboard.ScoreboardResponse;
import com.fijimf.deepfij.model.scraping.standings.StandingsResponse;
import com.fijimf.deepfij.model.scraping.team.RawTeam;
import com.fijimf.deepfij.model.scraping.team.Sport;
import com.fijimf.deepfij.model.scraping.team.SportsResponse;
import com.fijimf.deepfij.model.scraping.team.TeamWrapper;

@Component
public class ScrapingService {
    public static final Logger logger = LoggerFactory.getLogger(ScrapingService.class);
    private static final String CONFERENCES_API_URL = "https://site.web.api.espn.com/apis/site/v2/sports/basketball/mens-college-basketball/scoreboard/conferences";
    private static final String TEAMS_API_URL = "https://site.api.espn.com/apis/site/v2/sports/basketball/mens-college-basketball/teams?limit=500";
    private static final String TEAM_API_URL = "http://site.api.espn.com/apis/site/v2/sports/basketball/mens-college-basketball/teams/%s";
    private static final String STANDINGS_API_URL = "https://site.api.espn.com/apis/v2/sports/basketball/mens-college-basketball/standings?season=%d";
    private final static String SCOREBOARD_API_URL = "https://site.web.api.espn.com/apis/v2/scoreboard/header?sport=basketball&league=mens-college-basketball&limit=200&groups=50&dates=%d";

    public ScrapingService() {
    }

    public List<RawConference> fetchConferences() {
        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            // Sending HTTP GET request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CONFERENCES_API_URL))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the response is successful
            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                // Map JSON response into Java objects
                ObjectMapper mapper = new ObjectMapper();
                ConferenceResponse apiConferenceResponse = mapper.readValue(response.body(), ConferenceResponse.class);

                return apiConferenceResponse.conferences();
            } else {
                throw new RuntimeException("Failed to fetch data. HTTP status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error occurred while trying to scrap data from ESPN: " + e.getMessage(), e);
        }
    }

    public List<Sport> fetchTeams() {
        HttpClient client = HttpClient.newHttpClient();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TEAMS_API_URL))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                ObjectMapper mapper = new ObjectMapper();
                SportsResponse sportsResponse = mapper.readValue(response.body(), SportsResponse.class);

                return sportsResponse.sports();
            } else {
                throw new RuntimeException("Failed to fetch data. HTTP Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching teams: " + e.getMessage(), e);
        }
    }

    public RawTeam fetchTeamById(String id) {
        return fetchTeam(id);
    }

    public RawTeam fetchTeamBySlug(String slug) {
        return fetchTeam(slug);
    }

    private RawTeam fetchTeam(String p) {
        HttpClient client = HttpClient.newHttpClient();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TEAM_API_URL.formatted(p)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                ObjectMapper mapper = new ObjectMapper();
                TeamWrapper wrapper = mapper.readValue(response.body(), TeamWrapper.class);
                return wrapper.rawTeam();
            } else {
                throw new RuntimeException("Failed to fetch data. HTTP Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching teams: " + e.getMessage(), e);
        }

    }


    public StandingsResponse fetchStandings(int year) {
        HttpClient httpClient = HttpClient.newHttpClient();

        try {
            // Sending HTTP GET request with formatted URL including year
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(STANDINGS_API_URL.formatted(year)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the response is successful
            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                // Map JSON response into Java objects
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.body(), StandingsResponse.class);
            } else {
                throw new RuntimeException("Failed to fetch standings data. HTTP status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error occurred while trying to fetch standings from ESPN: " + e.getMessage(), e);
        }
    }

    public ScoreboardResponse fetchScoreboard(int date) {
        HttpClient client = HttpClient.newHttpClient();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SCOREBOARD_API_URL.formatted(date)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                // Map JSON response into Java objects
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                return mapper.readValue(response.body(), ScoreboardResponse.class);
            } else {
                logger.warn("Failed to fetch scoreboard data. HTTP status code: " + response.statusCode());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("Error occurred while trying to fetch scoreboard from ESPN: " + e.getMessage(), e);
            return null;
        }
    }
}

