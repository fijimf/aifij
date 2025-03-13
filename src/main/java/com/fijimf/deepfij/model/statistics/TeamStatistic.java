package com.fijimf.deepfij.model.statistics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "team_statistic")
public class TeamStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "season_id", nullable = false)
    private Long seasonId;

    @Column(name = "statistic_date", nullable = false)
    private LocalDate statisticDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statistic_type_id", nullable = false)
    private StatisticType statisticType;

    @Column(name = "numeric_value", precision = 12, scale = 4)
    private BigDecimal numericValue;

    @Column(name = "last_updated_at")
    private ZonedDateTime lastUpdatedAt;

    // Default constructor
    public TeamStatistic() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(Long seasonId) {
        this.seasonId = seasonId;
    }

    public LocalDate getStatisticDate() {
        return statisticDate;
    }

    public void setStatisticDate(LocalDate statisticDate) {
        this.statisticDate = statisticDate;
    }

    public StatisticType getStatisticType() {
        return statisticType;
    }

    public void setStatisticType(StatisticType statisticType) {
        this.statisticType = statisticType;
    }

    public BigDecimal getNumericValue() {
        return numericValue;
    }

    public void setNumericValue(BigDecimal numericValue) {
        this.numericValue = numericValue;
    }

    public ZonedDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(ZonedDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
} 