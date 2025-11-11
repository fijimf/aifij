package com.fijimf.deepfij.service;

import com.fijimf.deepfij.model.dto.gamedetail.*;
import com.fijimf.deepfij.model.schedule.*;
import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repo.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameDetailService {

    private static final Logger logger = LoggerFactory.getLogger(GameDetailService.class);

    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final ConferenceMappingRepository conferenceMappingRepository;
    private final TeamStatisticRepository teamStatisticRepository;
    private final StatisticTypeRepository statisticTypeRepository;

    public GameDetailService(
            GameRepository gameRepository,
            TeamRepository teamRepository,
            ConferenceMappingRepository conferenceMappingRepository,
            TeamStatisticRepository teamStatisticRepository,
            StatisticTypeRepository statisticTypeRepository) {
        this.gameRepository = gameRepository;
        this.teamRepository = teamRepository;
        this.conferenceMappingRepository = conferenceMappingRepository;
        this.teamStatisticRepository = teamStatisticRepository;
        this.statisticTypeRepository = statisticTypeRepository;
    }

    @Transactional(readOnly = true)
    public GameDetail getGameDetail(Long gameId) {
        long overallStart = System.currentTimeMillis();
        logger.info("Fetching game detail for game ID: {}", gameId);

        long dbStart = System.currentTimeMillis();
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with id: " + gameId));
        logger.debug("Game lookup completed in {} ms", System.currentTimeMillis() - dbStart);

        long gameInfoStart = System.currentTimeMillis();
        GameInfo gameInfo = buildGameInfo(game);
        logger.debug("buildGameInfo completed in {} ms", System.currentTimeMillis() - gameInfoStart);

        long homeTeamStart = System.currentTimeMillis();
        TeamDetail homeTeamDetail = buildTeamDetail(game.getHomeTeam(), game, true);
        logger.debug("buildTeamDetail (home) completed in {} ms", System.currentTimeMillis() - homeTeamStart);

        long awayTeamStart = System.currentTimeMillis();
        TeamDetail awayTeamDetail = buildTeamDetail(game.getAwayTeam(), game, false);
        logger.debug("buildTeamDetail (away) completed in {} ms", System.currentTimeMillis() - awayTeamStart);

        long overallDuration = System.currentTimeMillis() - overallStart;
        logger.info("getGameDetail completed in {} ms for game ID: {}", overallDuration, gameId);
        return new GameDetail(gameInfo, homeTeamDetail, awayTeamDetail);
    }

    private GameInfo buildGameInfo(Game game) {
        long start = System.currentTimeMillis();
        ConferenceInfo conferenceInfo = getConferenceForGame(game);
        logger.debug("  getConferenceForGame: {} ms", System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        HeadToHeadRecord headToHead = calculateHeadToHead(game);
        logger.debug("  calculateHeadToHead: {} ms", System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        List<PreviousMeeting> lastFiveMeetings = getLastFiveMeetings(game.getHomeTeam(), game.getAwayTeam(), game.getDate());
        logger.debug("  getLastFiveMeetings: {} ms", System.currentTimeMillis() - start);

        BettingLines bettingLines = extractBettingLines(game);
        PredictedValues predictions = calculatePredictions(game);

        return new GameInfo(
                game.getId(),
                game.getDate(),
                conferenceInfo,
                headToHead,
                lastFiveMeetings,
                bettingLines,
                predictions
        );
    }

    private TeamDetail buildTeamDetail(Team team, Game game, boolean isHomeTeam) {
        Season season = game.getSeason();
        LocalDate beforeDate = game.getDate();
        String teamType = isHomeTeam ? "home" : "away";

        long start = System.currentTimeMillis();
        ConferenceInfo conference = getTeamConference(team, season);
        logger.debug("  getTeamConference ({}): {} ms", teamType, System.currentTimeMillis() - start);

        Integer score = isHomeTeam ? game.getHomeScore() : game.getAwayScore();

        start = System.currentTimeMillis();
        WinLossRecord overallRecord = calculateOverallRecord(team, season, beforeDate);
        logger.debug("  calculateOverallRecord ({}): {} ms", teamType, System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        WinLossRecord conferenceRecord = calculateConferenceRecord(team, season, beforeDate);
        logger.debug("  calculateConferenceRecord ({}): {} ms", teamType, System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        WinLossRecord homeRecord = calculateHomeRecord(team, season, beforeDate);
        logger.debug("  calculateHomeRecord ({}): {} ms", teamType, System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        WinLossRecord awayRecord = calculateAwayRecord(team, season, beforeDate);
        logger.debug("  calculateAwayRecord ({}): {} ms", teamType, System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        WinLossRecord neutralRecord = calculateNeutralRecord(team, season, beforeDate);
        logger.debug("  calculateNeutralRecord ({}): {} ms", teamType, System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        WinLossRecord lastFiveRecord = calculateLastFiveRecord(team, season, beforeDate);
        logger.debug("  calculateLastFiveRecord ({}): {} ms", teamType, System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        Streak currentStreak = calculateCurrentStreak(team, season, beforeDate);
        logger.debug("  calculateCurrentStreak ({}): {} ms", teamType, System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        TeamStatistics statistics = getTeamStatistics(team, season, beforeDate);
        logger.debug("  getTeamStatistics ({}): {} ms", teamType, System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        List<TeamGameResult> seasonGames = getSeasonGames(team, season, beforeDate);
        logger.debug("  getSeasonGames ({}, {} games): {} ms", teamType, seasonGames.size(), System.currentTimeMillis() - start);

        return new TeamDetail(
                team.getId(),
                team.getName(),
                team.getAbbreviation(),
                team.getLogoUrl(),
                conference,
                score,
                overallRecord,
                conferenceRecord,
                homeRecord,
                awayRecord,
                neutralRecord,
                lastFiveRecord,
                currentStreak,
                statistics,
                seasonGames
        );
    }

    // ==================== Game-Level Utility Methods ====================

    private ConferenceInfo getConferenceForGame(Game game) {
        try {
            Conference homeConf = game.getHomeTeamConference();
            Conference awayConf = game.getAwayTeamConference();

            if (homeConf.equals(awayConf)) {
                return new ConferenceInfo(homeConf.getId(), homeConf.getName(), homeConf.getLogoUrl());
            }
        } catch (IllegalStateException e) {
            logger.debug("Teams are not in the same conference or conference mapping not found");
        }
        return null;
    }

    private HeadToHeadRecord calculateHeadToHead(Game game) {
        long dbStart = System.currentTimeMillis();
        List<Game> h2hGames = gameRepository.findBetweenTeamsInSeason(
                game.getHomeTeam(),
                game.getAwayTeam(),
                game.getSeason(),
                game.getDate()
        );
        logger.debug("    DB: findBetweenTeamsInSeason ({} games): {} ms", h2hGames.size(), System.currentTimeMillis() - dbStart);

        int homeWins = 0;
        int awayWins = 0;

        for (Game g : h2hGames) {
            if (g.isFinal()) {
                if (g.getHomeTeam().equals(game.getHomeTeam()) && g.isWinner(game.getHomeTeam())) {
                    homeWins++;
                } else if (g.getAwayTeam().equals(game.getHomeTeam()) && g.isWinner(game.getHomeTeam())) {
                    homeWins++;
                } else if (g.getHomeTeam().equals(game.getAwayTeam()) && g.isWinner(game.getAwayTeam())) {
                    awayWins++;
                } else if (g.getAwayTeam().equals(game.getAwayTeam()) && g.isWinner(game.getAwayTeam())) {
                    awayWins++;
                }
            }
        }

        return new HeadToHeadRecord(homeWins, awayWins);
    }

    private List<PreviousMeeting> getLastFiveMeetings(Team homeTeam, Team awayTeam, LocalDate beforeDate) {
        LocalDate tenYearsAgo = beforeDate.minusYears(10);
        long dbStart = System.currentTimeMillis();
        List<Game> meetings = gameRepository.findBetweenTeams(homeTeam, awayTeam, tenYearsAgo, beforeDate);
        logger.debug("    DB: findBetweenTeams ({} games): {} ms", meetings.size(), System.currentTimeMillis() - dbStart);

        return meetings.stream()
                .filter(Game::isFinal)
                .sorted((g1, g2) -> g2.getDate().compareTo(g1.getDate())) // descending
                .limit(5)
                .map(g -> new PreviousMeeting(
                        g.getDate(),
                        g.getHomeTeam().getName(),
                        g.getHomeTeam().getAbbreviation(),
                        g.getHomeScore(),
                        g.getAwayTeam().getName(),
                        g.getAwayTeam().getAbbreviation(),
                        g.getAwayScore()
                ))
                .collect(Collectors.toList());
    }

    private BettingLines extractBettingLines(Game game) {
        if (game.getSpread() == null && game.getOverUnder() == null &&
                game.getHomeMoneyLine() == null && game.getAwayMoneyLine() == null) {
            return null;
        }

        return new BettingLines(
                game.getSpread(),
                game.getOverUnder(),
                game.getHomeMoneyLine(),
                game.getAwayMoneyLine()
        );
    }

    private PredictedValues calculatePredictions(Game game) {
        // STUB: All prediction methods return null for now
        return new PredictedValues(null, null, null, null, null, null);
    }

    // ==================== Team-Level Utility Methods ====================

    private ConferenceInfo getTeamConference(Team team, Season season) {
        long dbStart = System.currentTimeMillis();
        List<ConferenceMapping> mappings = conferenceMappingRepository.findByTeamAndSeason(team, season);
        logger.debug("    DB: findByTeamAndSeason: {} ms", System.currentTimeMillis() - dbStart);

        if (mappings.isEmpty()) {
            return null;
        }

        Conference conference = mappings.get(0).getConference();
        return new ConferenceInfo(conference.getId(), conference.getName(), conference.getLogoUrl());
    }

    private WinLossRecord calculateOverallRecord(Team team, Season season, LocalDate beforeDate) {
        long dbStart = System.currentTimeMillis();
        List<Game> games = gameRepository.findByTeamAndSeasonBeforeDate(team, season, beforeDate);
        logger.debug("    DB: findByTeamAndSeasonBeforeDate ({} games): {} ms", games.size(), System.currentTimeMillis() - dbStart);
        return calculateWinLoss(games, team, g -> true);
    }

    private WinLossRecord calculateConferenceRecord(Team team, Season season, LocalDate beforeDate) {
        long dbStart = System.currentTimeMillis();
        List<Game> games = gameRepository.findByTeamAndSeasonBeforeDate(team, season, beforeDate);
        logger.debug("    DB: findByTeamAndSeasonBeforeDate ({} games): {} ms", games.size(), System.currentTimeMillis() - dbStart);

        dbStart = System.currentTimeMillis();
        Map<Team, Conference> conferenceMap = buildConferenceMap(season);
        logger.debug("    buildConferenceMap ({} teams): {} ms", conferenceMap.size(), System.currentTimeMillis() - dbStart);

        WinLossRecord record = calculateWinLoss(games, team, g -> g.isInConference(team, conferenceMap));

        // Return null if team is not in a conference
        if (record.wins() == 0 && record.losses() == 0 && conferenceMap.get(team) == null) {
            return null;
        }

        return record;
    }

    private WinLossRecord calculateHomeRecord(Team team, Season season, LocalDate beforeDate) {
        long dbStart = System.currentTimeMillis();
        List<Game> games = gameRepository.findByTeamAndSeasonBeforeDate(team, season, beforeDate);
        logger.debug("    DB: findByTeamAndSeasonBeforeDate ({} games): {} ms", games.size(), System.currentTimeMillis() - dbStart);
        return calculateWinLoss(games, team, g -> g.isHomeGame(team));
    }

    private WinLossRecord calculateAwayRecord(Team team, Season season, LocalDate beforeDate) {
        long dbStart = System.currentTimeMillis();
        List<Game> games = gameRepository.findByTeamAndSeasonBeforeDate(team, season, beforeDate);
        logger.debug("    DB: findByTeamAndSeasonBeforeDate ({} games): {} ms", games.size(), System.currentTimeMillis() - dbStart);
        return calculateWinLoss(games, team, g -> g.isAwayGame(team));
    }

    private WinLossRecord calculateNeutralRecord(Team team, Season season, LocalDate beforeDate) {
        long dbStart = System.currentTimeMillis();
        List<Game> games = gameRepository.findByTeamAndSeasonBeforeDate(team, season, beforeDate);
        logger.debug("    DB: findByTeamAndSeasonBeforeDate ({} games): {} ms", games.size(), System.currentTimeMillis() - dbStart);
        return calculateWinLoss(games, team, Game::isNeutralGame);
    }

    private WinLossRecord calculateLastFiveRecord(Team team, Season season, LocalDate beforeDate) {
        long dbStart = System.currentTimeMillis();
        List<Game> games = gameRepository.findByTeamAndSeasonBeforeDate(team, season, beforeDate);
        logger.debug("    DB: findByTeamAndSeasonBeforeDate ({} games): {} ms", games.size(), System.currentTimeMillis() - dbStart);

        List<Game> lastFive = games.stream()
                .filter(Game::isFinal)
                .sorted((g1, g2) -> g2.getDate().compareTo(g1.getDate())) // descending
                .limit(5)
                .toList();

        return calculateWinLoss(lastFive, team, g -> true);
    }

    private Streak calculateCurrentStreak(Team team, Season season, LocalDate beforeDate) {
        // Get streak from statistics WIN_STREAK and LOSS_STREAK
        StatisticValue winStreak = getStatisticValue(team, season, "WIN_STREAK", beforeDate);
        StatisticValue lossStreak = getStatisticValue(team, season, "LOSS_STREAK", beforeDate);

        if (winStreak != null && winStreak.value() != null && winStreak.value() > 0) {
            return new Streak(StreakType.WIN, winStreak.value().intValue());
        } else if (lossStreak != null && lossStreak.value() != null && lossStreak.value() > 0) {
            return new Streak(StreakType.LOSS, lossStreak.value().intValue());
        } else {
            return new Streak(StreakType.WIN, 0);
        }
    }

    private List<TeamGameResult> getSeasonGames(Team team, Season season, LocalDate beforeDate) {
        long dbStart = System.currentTimeMillis();
        List<Game> games = gameRepository.findByTeamAndSeasonBeforeDate(team, season, beforeDate);
        logger.debug("    DB: findByTeamAndSeasonBeforeDate ({} games): {} ms", games.size(), System.currentTimeMillis() - dbStart);

        return games.stream()
                .filter(Game::isFinal)
                .sorted((g1, g2) -> g2.getDate().compareTo(g1.getDate())) // descending
                .map(g -> {
                    Team opponent = g.getOpponent(team);
                    int teamScore = g.getScore(team);
                    int opponentScore = g.getScore(opponent);
                    boolean isWin = g.isWinner(team);

                    return new TeamGameResult(
                            g.getDate(),
                            opponent.getId(),
                            opponent.getName(),
                            opponent.getAbbreviation(),
                            teamScore,
                            opponentScore,
                            isWin
                    );
                })
                .collect(Collectors.toList());
    }

    // ==================== Statistics Utility Methods ====================

    private TeamStatistics getTeamStatistics(Team team, Season season, LocalDate beforeDate) {
        long statStart = System.currentTimeMillis();
        StatisticValue pointsForAvg = getStatisticValue(team, season, "POINTS_FOR_AVG", beforeDate);
        StatisticValue pointsForStdDev = getStatisticValue(team, season, "POINTS_FOR_SD", beforeDate);
        StatisticValue pointsAgainstAvg = getStatisticValue(team, season, "POINTS_AGAINST_AVG", beforeDate);
        StatisticValue pointsAgainstStdDev = getStatisticValue(team, season, "POINTS_AGAINST_SD", beforeDate);
        StatisticValue pointsCovariance = getStatisticValue(team, season, "PTS_FOR_PTS_AGAINST_COV", beforeDate);
        logger.debug("    Simple statistics (5 values): {} ms", System.currentTimeMillis() - statStart);

        statStart = System.currentTimeMillis();
        RankedStatistic linearRegression = getRankedStatistic(team, season, "LINEAR_REGRESSION", beforeDate);
        logger.debug("    LINEAR_REGRESSION (ranked): {} ms", System.currentTimeMillis() - statStart);

        statStart = System.currentTimeMillis();
        RankedStatistic logisticRegression = getRankedStatistic(team, season, "LOGISTIC_REGRESSION", beforeDate);
        logger.debug("    LOGISTIC_REGRESSION (ranked): {} ms", System.currentTimeMillis() - statStart);

        statStart = System.currentTimeMillis();
        RankedStatistic rpi = getRankedStatistic(team, season, "RPI", beforeDate);
        logger.debug("    RPI (ranked): {} ms", System.currentTimeMillis() - statStart);

        return new TeamStatistics(
                pointsForAvg,
                pointsForStdDev,
                pointsAgainstAvg,
                pointsAgainstStdDev,
                pointsCovariance,
                linearRegression,
                logisticRegression,
                rpi
        );
    }

    private StatisticValue getStatisticValue(Team team, Season season, String statKey, LocalDate beforeDate) {
        long dbStart = System.currentTimeMillis();
        Optional<StatisticType> statTypes = statisticTypeRepository.findByCode(statKey);
        if (statTypes.isEmpty()) {
            logger.debug("      DB: findByModelKey ({}): {} ms - NOT FOUND", statKey, System.currentTimeMillis() - dbStart);
            return new StatisticValue(null);
        }

        StatisticType statType = statTypes.get();
        List<TeamStatistic> stats = teamStatisticRepository.findForTeamLatestBeforeDate(team.getId(), season.getId(), statType.getId(), beforeDate);
        return stats.stream().findFirst().map(ts -> new StatisticValue(ts.getNumericValue() != null ? ts.getNumericValue().doubleValue() : null))
                .orElse(new StatisticValue(null));
    }

    private RankedStatistic getRankedStatistic(Team team, Season season, String modelKey, LocalDate beforeDate) {
        StatisticValue value = getStatisticValue(team, season, modelKey, beforeDate);
        return calculateRank(team, season, modelKey, beforeDate);
    }

    private RankedStatistic calculateRank(Team team, Season season, String modelKey, LocalDate beforeDate) {
        long dbStart = System.currentTimeMillis();
        List<StatisticType> statTypes = statisticTypeRepository.findByModelKey(modelKey);
        if (statTypes.isEmpty()) {
            logger.debug("      DB: findByModelKey ({}): {} ms - NOT FOUND", modelKey, System.currentTimeMillis() - dbStart);
            return null;
        }

        StatisticType statType = statTypes.get(0);

        // Get all team statistics for this statistic type and season
        long queryStart = System.currentTimeMillis();
        List<TeamStatistic> allStats =  statType.getIsHigherBetter()?
                teamStatisticRepository.findLatestBeforeDate(season.getId(), statType.getId(), beforeDate) :
                teamStatisticRepository.findLatestBeforeDate(season.getId(), statType.getId(), beforeDate).reversed();

        //Two passes
        TeamStatistic statistic = allStats.stream().filter(ts -> ts.getTeam().getId().equals(team.getId())).findFirst().orElse(null);
        long rank = allStats.stream().filter(ts -> {
            return statistic.getNumericValue() != null &&
                    (statType.getIsHigherBetter() ?
                            ts.getNumericValue().compareTo(statistic.getNumericValue()) > 0 :
                            ts.getNumericValue().compareTo(statistic.getNumericValue()) < 0);
        }).count() + 1;
        return new RankedStatistic(statistic.getNumericValue().doubleValue(), (int) rank);
    }

    // ==================== Helper Methods ====================

    private WinLossRecord calculateWinLoss(List<Game> games, Team team, java.util.function.Predicate<Game> filter) {
        int wins = 0;
        int losses = 0;

        for (Game game : games) {
            if (game.isFinal() && filter.test(game)) {
                if (game.isWinner(team)) {
                    wins++;
                } else if (game.isLoser(team)) {
                    losses++;
                }
            }
        }

        return new WinLossRecord(wins, losses);
    }

    private Map<Team, Conference> buildConferenceMap(Season season) {
        long dbStart = System.currentTimeMillis();
        List<ConferenceMapping> mappings = conferenceMappingRepository.findBySeason(season);
        logger.debug("      DB: findBySeason ({} mappings): {} ms", mappings.size(), System.currentTimeMillis() - dbStart);

        return mappings.stream()
                .collect(Collectors.toMap(
                        ConferenceMapping::getTeam,
                        ConferenceMapping::getConference,
                        (existing, replacement) -> existing
                ));
    }
}
