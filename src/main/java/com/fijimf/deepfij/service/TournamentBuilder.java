package com.fijimf.deepfij.service;

import com.fijimf.deepfij.model.dto.GameDTO;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.repo.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
public class TournamentBuilder {
    private final GameRepository gameRepository;

    @Autowired
    public TournamentBuilder(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Tournament build(Season season) {
        List<Game> games = gameRepository.findTournamentGamesBySeason(season);
        if (games.isEmpty()) {
            return new Tournament(Collections.emptyList());
        }
        LocalDate endDate = games.getFirst().getDate();


        List<TournamentEntry> nodes = new ArrayList<>();
        List<TournamentEntry> roots = new ArrayList<>();

        for (Game g : games) {
            TournamentEntry newNode = new TournamentEntry(g);
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

        public TournamentEntry(Game game) {
            this.game = GameDTO.fromGame(game);
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


