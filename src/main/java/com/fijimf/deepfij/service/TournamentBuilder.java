package com.fijimf.deepfij.service;

import com.fijimf.deepfij.model.dto.GameDTO;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.repo.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;


@Service
public class TournamentBuilder {
    private static final Logger log = LoggerFactory.getLogger(TournamentBuilder.class);
    private final GameRepository gameRepository;

    @Autowired
    public TournamentBuilder(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Tournament build(Season season) {
        String[] roundsByOrdinalDate = new String[]{"First Four", "First Four", "Round of 64", "Round of 64", "Round of 32", "Round of 32", "Sweet 16", "Elite 8", "Final 4", "National Championship"};
        List<Game> games = gameRepository.findTournamentGamesBySeason(season);
        if (games.isEmpty()) {
            return new Tournament(Collections.emptyList());
        }
        Map<LocalDate, String> dateToRound = new HashMap<>();
        List<LocalDate> dates = games.stream().map(Game::getDate).distinct().sorted(LocalDate::compareTo).toList();
        for (int index = 0; index < dates.size(); index++) {
            LocalDate date = dates.get(index);
            if (index < roundsByOrdinalDate.length) {
                dateToRound.put(date, roundsByOrdinalDate[index]);
            } else {
                log.warn("Too many dates constructing Tournament");
            }

        }


        List<TournamentEntry> nodes = new ArrayList<>();
        List<TournamentEntry> roots = new ArrayList<>();

        for (Game g : games) {
            TournamentEntry newNode = new TournamentEntry(g, dateToRound.getOrDefault(g.getDate(), null));
            boolean matched = false;
            for (TournamentEntry node : nodes) {
                if (node.getGame().homeTeam().id().equals(g.getWinner().getId()) && node.getHomeSource() == null) {
                    node.setHomeSource(newNode);
                    matched = true;
                } else if (node.getGame().awayTeam().id().equals(g.getWinner().getId()) && node.getAwaySource() == null) {
                    node.setAwaySource(newNode);
                    matched = true;
                }
            }
            nodes.add(newNode);
            if (!matched) {
                roots.add(newNode);
            }
        }

        return new Tournament(roots);

    }

    public record Tournament(List<TournamentEntry> roots) {
    }


    public static final class TournamentEntry {
        GameDTO game;
        TournamentEntry homeSource;
        TournamentEntry awaySource;
        String round;

        public TournamentEntry(Game game, String round) {
            this.game = GameDTO.fromGame(game, round);
        }

        public GameDTO getGame() {
            return game;
        }

        public TournamentEntry getHomeSource() {
            return homeSource;
        }

        public void setHomeSource(TournamentEntry homeSource) {
            this.homeSource = homeSource;
        }

        public TournamentEntry getAwaySource() {
            return awaySource;
        }

        public void setAwaySource(TournamentEntry awaySource) {
            this.awaySource = awaySource;
        }
    }


}


