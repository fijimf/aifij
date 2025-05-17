package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.schedule.*;
import com.fijimf.deepfij.model.schedule.Record;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public record TeamPage(Integer season, TeamDTO team, Map<String, Record> records, ConferenceDTO conference,
                       List<GameDTO> games) {

    public static TeamPage create(Team t, Season s) {
        List<Game> allGames = s.getGames();
        List<Game> games = allGames.stream().filter(game -> game.getHomeTeam().equals(t) || game.getAwayTeam().equals(t)).toList();
        Conference c = s.getConferenceMappings().stream().filter(mapping -> mapping.getTeam().equals(t)).map(ConferenceMapping::getConference).findFirst().orElseThrow(RuntimeException::new);
        Set<Team> conferenceTeams = s.getConferenceMappings()
                .stream()
                .filter(m -> Objects.equals(m.getConference(), c))
                .map(ConferenceMapping::getTeam)
                .collect(Collectors.toSet());
        List<Game> conferenceGames = allGames.stream().filter(game -> conferenceTeams.contains(game.getHomeTeam()) || conferenceTeams.contains(game.getAwayTeam())).toList();
        ConferenceStandings standings = ConferenceStandings.create(c, conferenceTeams, conferenceGames);

        TeamDTO team = TeamDTO.fromTeam(t);

        Map<String, Record> records = new HashMap<>();
        records.put("overall", Record.create(games, t, "Overall"));
        records.put("conference", Record.create(games.stream().filter(g -> conferenceTeams.contains(g.getHomeTeam()) && conferenceTeams.contains(g.getAwayTeam())).toList(), t, "Conference"));


        ConferenceDTO conference = new ConferenceDTO(c.getId(), c.getName(), c.getShortName(), c.getLogoUrl(), standings.standings().stream().map(standing -> new StandingDTO(TeamDTO.fromTeam(standing.team()), standing.conferenceRecord(), standing.overallRecord())).toList());
        Map<String, Object> map = new HashMap<>();
        map.put("conference", conference);

        List<GameDTO> gs = games.stream().map(game -> {
            Long id = game.getId();
            LocalDate date = game.getDate();
            Team opp = game.getOpponent(t);
            String atVs = (game.isNeutralGame() || game.isHomeGame(t)) ? "vs." : "@";
            Integer oppScore = game.getScore(opp);
            Integer score = game.getScore(t);
            String wOrL = game.isWinner(t) ? "W" : (game.isLoser(t) ? "L" : "");
            Double spread = game.getSpread();
            String spreadDescription = game.getSpreadDescription();
            boolean covered = game.covered(t);
            Double overUnder = game.getOverUnder();
            String overOrUnder = game.getOverOrUnder();
            Integer moneyLine = game.getMoneyLine(t);
            boolean moneyLinePaid = game.isWinner(t);
            Integer oppMoneyLine = game.getMoneyLine(opp);
            boolean oppMoneyLinePaid = game.isWinner(opp);
            return new GameDTO(id, TeamDTO.fromTeam(opp), atVs, game.isNeutralGame(), date, score, oppScore,wOrL, spread,
                    spreadDescription, covered, overUnder,overOrUnder, moneyLine,moneyLinePaid, oppMoneyLine,oppMoneyLinePaid);
        }).toList();

        return new TeamPage(s.getYear(), team, records, conference, gs);

    }

    public record ConferenceDTO(Long id, String name, String shortName, String logoUrl, List<StandingDTO> standings) {
    }

    public record StandingDTO(TeamDTO team, Record conferenceRecord, Record overallRecord) {
    }

    public record GameDTO(Long id, TeamDTO opponent, String atVs, boolean isNeutralSite, LocalDate date, Integer score,
                          Integer oppScore, String wOrL, Double spread, String spreadDescription, boolean spreadCovered,
                          Double overUnder, String overOrUnder, Integer moneyLine, boolean moneyLinePaid,
                          Integer oppMoneyLine, boolean oppMoneyLinePaid) {
    }
}
