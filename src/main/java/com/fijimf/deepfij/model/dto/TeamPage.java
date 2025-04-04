package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.schedule.*;
import com.fijimf.deepfij.model.schedule.Record;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public record TeamPage(Integer season, TeamDTO team, Map<String, Record> records, ConferenceDTO conference,
                       List<GameDTO> games) {

    public static TeamPage create(Team t, Season s) {
        List<Game> games = s.getGames().stream().filter(game -> game.getHomeTeam().equals(t) || game.getAwayTeam().equals(t)).toList();
        Conference c = s.getConferenceMappings().stream().filter(mapping -> mapping.getTeam().equals(t)).map(ConferenceMapping::getConference).findFirst().orElseThrow(RuntimeException::new);
        Set<com.fijimf.deepfij.model.schedule.Team> conferenceTeams = s.getConferenceMappings()
                .stream()
                .filter(m -> Objects.equals(m.getConference(), c))
                .map(ConferenceMapping::getTeam)
                .collect(Collectors.toSet());
        ConferenceStandings standings = ConferenceStandings.create(c, conferenceTeams, games);

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
            Double spread = game.getSpread();
            Double overUnder = game.getOverUnder();
            Integer moneyLine = game.getMoneyLine(t);
            Integer oppMoneyLine = game.getMoneyLine(opp);
            return new GameDTO(id, TeamDTO.fromTeam(opp), atVs, game.isNeutralGame(), date, score, oppScore, spread, overUnder, moneyLine, oppMoneyLine);
        }).toList();

        return new TeamPage(s.getYear(), team, records, conference, gs);

    }

    public record TeamDTO(Long id, String name, String longName, String nickname, String logoUrl, String primaryColor,
                          String secondaryColor, String slug) {
        public static TeamDTO fromTeam(Team team) {
            return new TeamDTO(
                    team.getId(),
                    team.getName(),
                    team.getLongName(),
                    team.getNickname(),
                    team.getLogoUrl(),
                    team.getPrimaryColor(),
                    team.getSecondaryColor(),
                    team.getSlug());
        }
    }

    public record ConferenceDTO(Long id, String name, String shortName, String logoUrl, List<StandingDTO> standings) {
    }

    public record StandingDTO(TeamDTO team, Record conferenceRecord, Record overallRecord) {
    }

    public record GameDTO(Long id, TeamDTO opponent, String atVs, boolean isNeutralSite, LocalDate date, Integer score,
                          Integer oppScore, Double spread, Double overUnder, Integer moneyLine, Integer oppMoneyLine) {
    }
}
