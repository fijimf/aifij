package com.fijimf.deepfij.service.stat;

import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repo.GameRepository;
import com.fijimf.deepfij.service.StatisticTypeService;
import com.fijimf.deepfij.service.StatisticalModel;
import com.fijimf.deepfij.service.TeamStatisticBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WonLostStatisticModel implements StatisticalModel {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private StatisticTypeService statisticTypeService;

    @Override
    public String key() {
        return "WONLOST";
    }

    public static final String WINS_DESCRIPTION = """
            Wins is simply the number of wins that a team has as of that date.
            It is a naive statistic, but all else equal the team with more wins will be more likely to beat one with fewer.""";
    public static final String LOSSES_DESCRIPTION = """
            Losses is simply the number of losses that a team has as of that date.
            It is a naive statistic, but all else equal the team with fewer losses will be more likely to beat one with more.""";
    public static final String WIN_STREAK_DESCRIPTION = """
            Winning streak is the number of games a team has won in a row.  We do not carry streaks across seasons.
            "At the margins, winning streak conveys some information, but is unlikely to be effective for prediction.""";
    public static final String LOSS_STREAK_DESCRIPTION = """
            Losing streak is the number of games a team has won in a row.  We do not carry streaks across seasons.
            At the margins, losing streak conveys some information, but is unlikely to be effective for prediction.""";
    public static final String WIN_PCT_DESCRIPTION = """
            Winning percent is the number of wins divided by the number of games that a team has played as of that date.
            It is an improvement over wins and losses, but does not account for the quality of the games.  All else equal the team with a higher winning percent will be bore likely to beat one with fewer.""";
    ;
    public static final String WINS_KEY = "WINS";
    public static final String LOSSES_KEY = "LOSSES";
    public static final String WIN_STREAK_KEY = "WIN_STREAK";
    public static final String LOSS_STREAK_KEY = "LOSS_STREAK";
    public static final String WIN_PCT_KEY = "WIN_PCT";

    @Override
    public List<StatisticType> refreshDBTypes() {
        return List.of(statisticTypeService.findOrCreateStatisticType(WINS_KEY, "WINS", WINS_DESCRIPTION, true, 0, key()),
                statisticTypeService.findOrCreateStatisticType(LOSSES_KEY, "LOSSES", LOSSES_DESCRIPTION, false, 0, key()),
                statisticTypeService.findOrCreateStatisticType(WIN_STREAK_KEY, "WIN_STREAK", WIN_STREAK_DESCRIPTION, true, 0, key()),
                statisticTypeService.findOrCreateStatisticType(LOSS_STREAK_KEY, "LOSS_STREAK", LOSS_STREAK_DESCRIPTION, false, 0, key()),
                statisticTypeService.findOrCreateStatisticType(WIN_PCT_KEY, "WIN_PCT", WIN_PCT_DESCRIPTION, true, 4, key()));
    }

    @Override
    public List<TeamStatistic> generate(Season season) {
        StatisticType winsType = statisticTypeService.findStatisticType(WINS_KEY);
        StatisticType lossesType = statisticTypeService.findStatisticType(LOSSES_KEY);
        StatisticType winStreakType = statisticTypeService.findStatisticType(WIN_STREAK_KEY);
        StatisticType lossStreakType = statisticTypeService.findStatisticType(LOSS_STREAK_KEY);
        StatisticType winningPctType = statisticTypeService.findStatisticType(WIN_PCT_KEY);

        // Get all games for the season ordered by date
        List<Game> games = gameRepository.findBySeasonOrderByDateAsc(season);

        // Map to track running totals for each team
        Map<Team, Integer> wins = new HashMap<>();
        Map<Team, Integer> losses = new HashMap<>();
        Map<Team, Integer> winStreak = new HashMap<>();
        Map<Team, Integer> lossStreak = new HashMap<>();
        Map<Team, Double> winningPct = new HashMap<>();
        List<TeamStatistic> statistics = new ArrayList<>();
        // Process each game and update running totals

        Map<LocalDate, List<Game>> gamesByDate = games.stream().filter(Game::isComplete).collect(Collectors.groupingBy(Game::getDate));
        LocalDate lastDate = gamesByDate.keySet().stream().max(LocalDate::compareTo).orElseThrow();
        LocalDate startDate = gamesByDate.keySet().stream().min(LocalDate::compareTo).orElseThrow();
        for (LocalDate date = startDate; !date.isAfter(lastDate); date = date.plusDays(1)) {
            List<Game> dayGames = gamesByDate.getOrDefault(date, Collections.emptyList());
            for (Game game : dayGames) {
                if (game.isComplete()) {
                    Team winner;
                    Team loser;
                    if (game.getHomeScore() > game.getAwayScore()) {
                        winner = game.getHomeTeam();
                        loser = game.getAwayTeam();
                    } else {
                        winner = game.getAwayTeam();
                        loser = game.getHomeTeam();
                    }

                    wins.put(winner, wins.getOrDefault(winner, 0) + 1);
                    wins.putIfAbsent(loser, 0);
                    losses.put(loser, losses.getOrDefault(loser, 0) + 1);
                    losses.putIfAbsent(winner, 0);
                    winStreak.put(winner, winStreak.getOrDefault(winner, 0) + 1);
                    winStreak.put(loser, 0);
                    lossStreak.put(loser, lossStreak.getOrDefault(loser, 0) + 1);
                    lossStreak.put(winner, 0);

                    winningPct.put(winner, (double) wins.get(winner) / (wins.get(winner) + losses.get(winner)));
                    winningPct.put(loser, (double) wins.get(loser) / (wins.get(loser) + losses.get(loser)));
                }
            }

            for (Map.Entry<Team, Integer> entry : wins.entrySet()) {
                Team team = entry.getKey();
                TeamStatisticBuilder base = new TeamStatisticBuilder().withTeam(team).withSeason(season).withDate(date);
                statistics.add(base.withType(winsType).withValue(BigDecimal.valueOf(wins.get(team))).build());
                statistics.add(base.withType(lossesType).withValue(BigDecimal.valueOf(losses.get(team))).build());
                statistics.add(base.withType(winStreakType).withValue(BigDecimal.valueOf(winStreak.get(team))).build());
                statistics.add(base.withType(lossStreakType).withValue(BigDecimal.valueOf(lossStreak.get(team))).build());
                statistics.add(base.withType(winningPctType).withValue(BigDecimal.valueOf(winningPct.get(team))).build());
            }
        }
        return statistics;
    }
}
