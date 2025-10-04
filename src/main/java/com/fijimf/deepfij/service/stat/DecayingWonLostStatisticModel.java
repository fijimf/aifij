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
public class DecayingWonLostStatisticModel implements StatisticalModel {

    private static final double DECAY_FACTOR = 0.975;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private StatisticTypeService statisticTypeService;

    @Override
    public String key() {
        return "DECAYING_WONLOST";
    }

    public static final String DECAYING_WINS_DESCRIPTION = """
            Decaying wins applies a daily decay factor to the win total, giving more weight to recent wins.
            Each day without a game, the previous day's total is multiplied by the decay factor. When a win occurs, 1 is added to the decayed total.
            This metric emphasizes recent performance over historical results.""";

    public static final String DECAYING_LOSSES_DESCRIPTION = """
            Decaying losses applies a daily decay factor to the loss total, giving more weight to recent losses.
            Each day without a game, the previous day's total is multiplied by the decay factor. When a loss occurs, 1 is added to the decayed total.
            This metric emphasizes recent performance over historical results.""";

    public static final String DECAYING_WIN_PCT_DESCRIPTION = """
            Decaying winning percentage is calculated from decaying wins and decaying losses.
            It represents the ratio of decayed wins to total decayed games, emphasizing recent performance.
            This provides a more time-sensitive measure of team strength than traditional winning percentage.""";

    public static final String DECAYING_WINS_KEY = "DECAYING_WINS";
    public static final String DECAYING_LOSSES_KEY = "DECAYING_LOSSES";
    public static final String DECAYING_WIN_PCT_KEY = "DECAYING_WIN_PCT";

    @Override
    public List<StatisticType> refreshDBTypes() {
        return List.of(
                statisticTypeService.findOrCreateStatisticType(DECAYING_WINS_KEY, "DECAYING_WINS", DECAYING_WINS_DESCRIPTION, true, 3, key()),
                statisticTypeService.findOrCreateStatisticType(DECAYING_LOSSES_KEY, "DECAYING_LOSSES", DECAYING_LOSSES_DESCRIPTION, false, 3, key()),
                statisticTypeService.findOrCreateStatisticType(DECAYING_WIN_PCT_KEY, "DECAYING_WIN_PCT", DECAYING_WIN_PCT_DESCRIPTION, true, 4, key())
        );
    }

    @Override
    public List<TeamStatistic> generate(Season season) {
        StatisticType decayingWinsType = statisticTypeService.findStatisticType(DECAYING_WINS_KEY);
        StatisticType decayingLossesType = statisticTypeService.findStatisticType(DECAYING_LOSSES_KEY);
        StatisticType decayingWinPctType = statisticTypeService.findStatisticType(DECAYING_WIN_PCT_KEY);

        // Get all games for the season ordered by date
        List<Game> games = gameRepository.findBySeasonOrderByDateAsc(season);

        // Map to track running totals for each team with decay
        Map<Team, Double> decayingWins = new HashMap<>();
        Map<Team, Double> decayingLosses = new HashMap<>();
        Map<Team, Double> decayingWinPct = new HashMap<>();
        List<TeamStatistic> statistics = new ArrayList<>();

        // Process each game and update running totals with decay
        Map<LocalDate, List<Game>> gamesByDate = games.stream()
                .filter(Game::isComplete)
                .collect(Collectors.groupingBy(Game::getDate));

        LocalDate lastDate = gamesByDate.keySet().stream().max(LocalDate::compareTo).orElseThrow();
        LocalDate startDate = gamesByDate.keySet().stream().min(LocalDate::compareTo).orElseThrow();

        for (LocalDate date = startDate; !date.isAfter(lastDate); date = date.plusDays(1)) {
            // Apply decay to all teams that have played
            for (Team team : decayingWins.keySet()) {
                decayingWins.put(team, decayingWins.get(team) * DECAY_FACTOR);
                decayingLosses.put(team, decayingLosses.get(team) * DECAY_FACTOR);
            }

            // Process games for this date
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

                    // Add 1 to the decayed total for the winner
                    decayingWins.put(winner, decayingWins.getOrDefault(winner, 0.0) + 1.0);
                    decayingWins.putIfAbsent(loser, 0.0);

                    // Add 1 to the decayed total for the loser
                    decayingLosses.put(loser, decayingLosses.getOrDefault(loser, 0.0) + 1.0);
                    decayingLosses.putIfAbsent(winner, 0.0);

                    // Calculate decaying winning percentage
                    double totalGames = decayingWins.get(winner) + decayingLosses.get(winner);
                    decayingWinPct.put(winner, totalGames > 0 ? decayingWins.get(winner) / totalGames : 0.0);

                    totalGames = decayingWins.get(loser) + decayingLosses.get(loser);
                    decayingWinPct.put(loser, totalGames > 0 ? decayingWins.get(loser) / totalGames : 0.0);
                }
            }

            // Record statistics for all teams that have played
            for (Map.Entry<Team, Double> entry : decayingWins.entrySet()) {
                Team team = entry.getKey();
                TeamStatisticBuilder base = new TeamStatisticBuilder()
                        .withTeam(team)
                        .withSeason(season)
                        .withDate(date);

                statistics.add(base.withType(decayingWinsType)
                        .withValue(BigDecimal.valueOf(decayingWins.get(team)))
                        .build());
                statistics.add(base.withType(decayingLossesType)
                        .withValue(BigDecimal.valueOf(decayingLosses.get(team)))
                        .build());
                statistics.add(base.withType(decayingWinPctType)
                        .withValue(BigDecimal.valueOf(decayingWinPct.getOrDefault(team, 0.0)))
                        .build());
            }
        }

        return statistics;
    }
}
