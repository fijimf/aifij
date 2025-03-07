package com.fijimf.deepfij.model.schedule;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

    // Enforce hame_team_id != away_team_id
    public void validateGameTeams() {
        if (homeTeam.equals(awayTeam)) {
            throw new IllegalArgumentException("Home team and away team must be different");
        }
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
}