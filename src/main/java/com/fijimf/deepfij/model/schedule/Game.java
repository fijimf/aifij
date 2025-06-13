package com.fijimf.deepfij.model.schedule;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @NotNull
    @Column(name = "espn_id", nullable = false)
    private String espnId;

    @NotNull
    @Column(name = "index_date", nullable = false)
    private LocalDate indexDate;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "time")
    private LocalTime time;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Size(max = 20)
    @Column(name = "status")
    private String status;

    @Column(name = "periods")
    private Integer periods;

    @Size(max = 100)
    @Column(name = "location")
    private String location;

    @Column(name = "neutral_site")
    private Boolean neutralSite = false;

    @Column(name = "home_team_seed")
    private Integer homeTeamSeed;

    @Column(name = "away_team_seed")
    private Integer awayTeamSeed;

    @Column(name = "spread")
    private Double spread;

    @Column(name = "over_under")
    private Double overUnder;

    @Column(name = "home_money_line")
    private Integer homeMoneyLine;

    @Column(name = "away_money_line")
    private Integer awayMoneyLine;

    // Default constructor
    public Game() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEspnId() {
        return espnId;
    }

    public void setEspnId(String espnId) {
        this.espnId = espnId;
    }

    public LocalDate getIndexDate() {
        return indexDate;
    }

    public void setIndexDate(LocalDate indexDate) {
        this.indexDate = indexDate;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPeriods() {
        return periods;
    }

    public void setPeriods(Integer periods) {
        this.periods = periods;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getNeutralSite() {
        return neutralSite;
    }

    public void setNeutralSite(Boolean neutralSite) {
        this.neutralSite = neutralSite;
    }

    public Integer getHomeTeamSeed() {
        return homeTeamSeed;
    }

    public void setHomeTeamSeed(Integer homeTeamSeed) {
        this.homeTeamSeed = homeTeamSeed;
    }

    public Integer getAwayTeamSeed() {
        return awayTeamSeed;
    }

    public void setAwayTeamSeed(Integer awayTeamSeed) {
        this.awayTeamSeed = awayTeamSeed;
    }

    public Double getSpread() {
        return spread;
    }

    public void setSpread(Double spread) {
        this.spread = spread;
    }

    public Double getOverUnder() {
        return overUnder;
    }

    public void setOverUnder(Double overUnder) {
        this.overUnder = overUnder;
    }

    public Integer getHomeMoneyLine() {
        return homeMoneyLine;
    }

    public void setHomeMoneyLine(Integer homeMoneyLine) {
        this.homeMoneyLine = homeMoneyLine;
    }

    public Integer getAwayMoneyLine() {
        return awayMoneyLine;
    }

    public void setAwayMoneyLine(Integer awayMoneyLine) {
        this.awayMoneyLine = awayMoneyLine;
    }

    public boolean isComplete() {
        return homeScore != null && awayScore != null && homeScore > 0 && awayScore > 0;
    }

    public static Game update(Game fromDb, Game scrapeGame) {
        if (fromDb == null) {
            throw new IllegalArgumentException("Cannot copy from null Game");
        }
        boolean needsUpdate = false;

        if (!Objects.equals(fromDb.time, scrapeGame.time)) {
            fromDb.time = scrapeGame.time;
            needsUpdate = true;
        }

        if (!Objects.equals(fromDb.date, scrapeGame.date)) {
            fromDb.date = scrapeGame.date;
            needsUpdate = true;
        }

        if (!Objects.equals(fromDb.homeScore, scrapeGame.homeScore)) {
            fromDb.homeScore = scrapeGame.homeScore;
            needsUpdate = true;
        }
        if (!Objects.equals(fromDb.awayScore, scrapeGame.awayScore)) {
            fromDb.awayScore = scrapeGame.awayScore;
            needsUpdate = true;
        }
        if (!Objects.equals(fromDb.status, scrapeGame.status)) {
            fromDb.status = scrapeGame.status;
            needsUpdate = true;
        }
        if (!Objects.equals(fromDb.periods, scrapeGame.periods)) {
            fromDb.periods = scrapeGame.periods;
            needsUpdate = true;
        }
        if (!Objects.equals(fromDb.location, scrapeGame.location)) {
            fromDb.location = scrapeGame.location;
            needsUpdate = true;
        }
        if (!Objects.equals(fromDb.neutralSite, scrapeGame.neutralSite)) {
            fromDb.neutralSite = scrapeGame.neutralSite;
            needsUpdate = true;
        }
        if (!Objects.equals(fromDb.homeTeamSeed, scrapeGame.homeTeamSeed)) {
            fromDb.homeTeamSeed = scrapeGame.homeTeamSeed;
            needsUpdate = true;
        }
        if (!Objects.equals(fromDb.awayTeamSeed, scrapeGame.awayTeamSeed)) {
            fromDb.awayTeamSeed = scrapeGame.awayTeamSeed;
            needsUpdate = true;
        }
        if (!Objects.equals(fromDb.spread, scrapeGame.spread)) {
            fromDb.spread = scrapeGame.spread;
            needsUpdate = true;
        }
        if (!Objects.equals(fromDb.overUnder, scrapeGame.overUnder)) {
            fromDb.overUnder = scrapeGame.overUnder;
            needsUpdate = true;
        }
        if (!Objects.equals(fromDb.homeMoneyLine, scrapeGame.homeMoneyLine)) {
            fromDb.homeMoneyLine = scrapeGame.homeMoneyLine;
            needsUpdate = true;
        }
        if (!Objects.equals(fromDb.awayMoneyLine, scrapeGame.awayMoneyLine)) {
            fromDb.awayMoneyLine = scrapeGame.awayMoneyLine;
            needsUpdate = true;
        }
        if (needsUpdate) {
            return fromDb;
        } else {
            return null;
        }
    }

    public Team getWinner() {
        if (homeScore > awayScore) {
            return homeTeam;
        } else if (homeScore < awayScore) {
            return awayTeam;
        } else {
            return null;
        }
    }

    public Team getLoser() {
        if (homeScore > awayScore) {
            return awayTeam;
        } else if (homeScore < awayScore) {
            return homeTeam;
        } else {
            return null;
        }
    }

    public boolean hasTeam(Team team) {
        return Objects.equals(homeTeam, team) || Objects.equals(awayTeam, team);
    }

    public boolean isHomeGame(Team team) {
        return Objects.equals(homeTeam, team) && !neutralSite;
    }

    public boolean isAwayGame(Team team) {
        return Objects.equals(awayTeam, team) && !neutralSite;
    }

    public boolean isNeutralGame() {
        return neutralSite;
    }

    public boolean isFinal() {
        return homeScore != null && awayScore != null && homeScore > 0 && awayScore > 0;
    }

    public boolean isWinner(Team team) {
        return isFinal() && Objects.equals(team, getWinner());
    }

    public boolean isLoser(Team team) {
        return isFinal() && Objects.equals(team, getLoser());
    }

    public Team getOpponent(Team t) {
        if (homeTeam.equals(t)) {
            return awayTeam;
        } else if (awayTeam.equals(t)) {
            return homeTeam;
        } else {
            throw new IllegalArgumentException("Team " + t.getName() + " is not a game participant");
        }
    }

    public Integer getScore(Team team) {
        if (homeTeam.equals(team)) {
            return homeScore;
        } else if (awayTeam.equals(team)) {
            return awayScore;
        } else {
            throw new IllegalArgumentException("Team " + team.getName() + " is not a game participant");
        }

    }

    public Integer getMoneyLine(Team team) {
        if (homeTeam.equals(team)) {
            return homeMoneyLine;
        } else if (awayTeam.equals(team)) {
            return awayMoneyLine;
        } else {
            throw new IllegalArgumentException("Team " + team.getName() + " is not a game participant");
        }
    }

    public String getOverOrUnder() {
        if (overUnder != null && isFinal()) {
            if (homeScore + awayScore > overUnder) {
                return "OVER";
            } else if (homeScore + awayScore < overUnder) {
                return "UNDER";
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean covered(Team team) {
        if (isFinal() && spread != null) {
            if (homeTeam.equals(team)) {
                return (homeScore - awayScore > spread);
            } else if (awayTeam.equals(team)) {
                return (awayScore - homeScore > spread);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String getSpreadDescription() {
        return spread == null ? "" : "%s%+4.1f".formatted(homeTeam.getAbbreviation(), spread);
    }

    public Conference getHomeTeamConference() {
        for (ConferenceMapping cm : this.season.getConferenceMappings()) {
            if (cm.getTeam().equals(this.homeTeam)) {
                return cm.getConference();
            }
        }
        throw new IllegalStateException("Home team " + this.homeTeam.getName() + " is not mapped to a conference");
    }
    public Conference getAwayTeamConference() {
        for (ConferenceMapping cm : this.season.getConferenceMappings()) {
            if (cm.getTeam().equals(this.awayTeam)) {
                return cm.getConference();
            }
        }
        throw new IllegalStateException("Away team " + this.awayTeam.getName() + " is not mapped to a conference");
    }

    public boolean isConferenceGame(){
        return getHomeTeamConference().equals(getAwayTeamConference()); }
}