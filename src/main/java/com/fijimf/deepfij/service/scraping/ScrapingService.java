package com.fijimf.deepfij.service.scraping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fijimf.deepfij.model.scraping.conference.Conference;
import com.fijimf.deepfij.model.scraping.conference.ConferenceResponse;
import com.fijimf.deepfij.model.scraping.scoreboard.ScoreboardResponse;
import com.fijimf.deepfij.model.scraping.standings.StandingsResponse;
import com.fijimf.deepfij.model.scraping.team.Sport;
import com.fijimf.deepfij.model.scraping.team.SportsResponse;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public class ScrapingService {

    private static final String CONFERENCES_API_URL = "https://site.web.api.espn.com/apis/site/v2/sports/basketball/mens-college-basketball/scoreboard/conferences";
    private static final String TEAMS_API_URL = "https://site.api.espn.com/apis/site/v2/sports/basketball/mens-college-basketball/teams?limit=500";
    private static final String STANDINGS_API_URL = "https://site.api.espn.com/apis/v2/sports/basketball/mens-college-basketball/standings?season=%d";
    private final static String SCOREBOARD_API_URL = "https://site.web.api.espn.com/apis/v2/scoreboard/header?sport=basketball&league=mens-college-basketball&limit=200&groups=50&dates=%d";

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

                return apiConferenceResponse.conferences();
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

                return sportsResponse.sports();
            } else {
                throw new RuntimeException("Failed to fetch data. HTTP Status: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching teams: " + e.getMessage(), e);
        }
    }

    public StandingsResponse fetchStandings(int year) {
        HttpClient httpClient = HttpClient.newHttpClient();

        try {
            // Sending HTTP GET request with formatted URL including year
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URL(String.format(STANDINGS_API_URL, year)).toURI())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the response is successful
            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                // Map JSON response into Java objects
                ObjectMapper mapper = new ObjectMapper();
                StandingsResponse standingsResponse = mapper.readValue(response.body(), StandingsResponse.class);

                return standingsResponse;
            } else {
                throw new RuntimeException("Failed to fetch standings data. HTTP status code: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while trying to fetch standings from ESPN: " + e.getMessage(), e);
        }
    }

    public ScoreboardResponse fetchScoreboard(int date) {
        HttpClient client = HttpClient.newHttpClient();

        try {
            // Step 2: Create an HttpClient instance


            // Step 3: Create an HttpRequest object
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URL(String.format(SCOREBOARD_API_URL, date)).toURI())
                    .build();

            // Step 4: Send the request and capture the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                // Map JSON response into Java objects
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                ScoreboardResponse scoreboardResponse = mapper.readValue(response.body(), ScoreboardResponse.class);

                return scoreboardResponse;
            } else {
                throw new RuntimeException("Failed to fetch scoreboard data. HTTP status code: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while trying to fetch scoreboard from ESPN: " + e.getMessage(), e);
        }
    }


    public static void main(String[] args) {
        ScrapingService service = new ScrapingService();
        ScoreboardResponse response = service.fetchScoreboard(20241123);

        // Print teams
        response.sports().forEach(sport -> {
            System.out.println(sport.name());
            sport.leagues().forEach(league -> {
                System.out.println(league.shortName());
                System.out.println(league.isTournament());
                league.events().forEach(event->{
                    System.out.println(event.shortName());
                    System.out.println(event.neutralSite()+" -- "+event.note());
                    event.competitors().forEach(comp->{
                        System.out.println(comp.name()+","+ comp.tournamentMatchup());
                    });
                });
            });
        });
    }
//        StandingsResponse response = service.fetchStandings(2014);
//
//        // Print teams
//        response.getChildren().forEach(confStanding -> {
//            System.out.println(confStanding.getName());
//            confStanding.consolidatedStandings().forEach(entry -> {
//                System.out.println(entry.getTeam().getDisplayName());
//            });
//        });
//    }

//    public static void main(String[] args) {
//        ScrapingService service = new ScrapingService();
//        List<Sport> sports = service.fetchTeams();
//
//        // Print teams
//        sports.forEach(sport -> {
//            sport.getLeagues().forEach(league -> {
//                league.getTeams().forEach(teamWrapper -> System.out.println(teamWrapper.getTeam().getDisplayName()));
//            });
//        });
//    }

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

