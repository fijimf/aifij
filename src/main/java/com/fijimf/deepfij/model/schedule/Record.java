package com.fijimf.deepfij.model.schedule;

import java.util.Comparator;
import java.util.List;

public record Record(String name, int wins, int losses) {
    public static Record create(List<Game> games, Team team, String name) {
        long wins = games.stream().filter(g -> g.isWinner(team)).count();
        long losses = games.stream().filter(g -> g.isLoser(team)).count();
        return new Record(name, Math.toIntExact(wins), Math.toIntExact(losses));
    }

    public static Record create(List<Game> games, Team team) {
        return create(games, team, null);
    }

    public int gamesOver() {
        return wins - losses;
    }

    public static final Comparator<Record> NATURAL_ORDER =
            Comparator
                    .comparing(Record::gamesOver)
                    .thenComparing(Record::wins);

}
