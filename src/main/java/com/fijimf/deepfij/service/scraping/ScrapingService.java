package com.fijimf.deepfij.service.scraping;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ScrapingService {

    private static final String CONFERENCES_API_URL = "https://site.web.api.espn.com/apis/site/v2/sports/basketball/mens-college-basketball/scoreboard/conferences";
    private static final String TEAMS_API_URL = "https://site.api.espn.com/apis/site/v2/sports/basketball/mens-college-basketball/teams?limit=500";

    public List<Conference> fetchConferences() {
        HttpClient httpClient = HttpClient.newHttpClient();

        try {
            // Sending HTTP GET request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URL(CONFERENCES_API_URL).toURI())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the response is successful
            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                // Map JSON response into Java objects
                ObjectMapper mapper = new ObjectMapper();
                ConferenceResponse apiConferenceResponse = mapper.readValue(response.body(), ConferenceResponse.class);

                return apiConferenceResponse.getConferences();
            } else {
                throw new RuntimeException("Failed to fetch data. HTTP status code: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while trying to scrap data from ESPN: " + e.getMessage(), e);
        }
    }


    public List<Sport> fetchTeams() {
        HttpClient client = HttpClient.newHttpClient();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URL(TEAMS_API_URL).toURI())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                ObjectMapper mapper = new ObjectMapper();
                SportsResponse sportsResponse = mapper.readValue(response.body(), SportsResponse.class);

                return sportsResponse.getSports();
            } else {
                throw new RuntimeException("Failed to fetch data. HTTP Status: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching teams: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        ScrapingService service = new ScrapingService();
        List<Sport> sports = service.fetchTeams();

        // Print teams
        sports.forEach(sport -> {
            sport.getLeagues().forEach(league -> {
                league.getTeams().forEach(teamWrapper -> System.out.println(teamWrapper.getTeam().getDisplayName()));
            });
        });
    }

//    // Main method for simple testing
//    public static void main(String[] args) {
//        ScrapingService scrapingService = new ScrapingService();
//        List<Conference> conferences = scrapingService.fetchConferences();
//
//        // Print the fetched list of conferences
//        for (Conference conference : conferences) {
//            System.out.println(conference);
//        }
//    }
}

