package com.fijimf.deepfij.model.statistics;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Entity
@Table(name = "statistic_type")
public class StatisticType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "is_higher_better")
    private Boolean isHigherBetter = true;

    @Column(name = "decimal_places")
    private int decimalPlaces = 2;

    @OneToMany(mappedBy = "statisticType", fetch = FetchType.LAZY)
    private Set<TeamStatistic> teamStatistics;

    // Default constructor
    public StatisticType() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsHigherBetter() {
        return isHigherBetter;
    }

    public void setIsHigherBetter(Boolean isHigherBetter) {
        this.isHigherBetter = isHigherBetter;
    }

    public Set<TeamStatistic> getTeamStatistics() {
        return teamStatistics;
    }

    public void setTeamStatistics(Set<TeamStatistic> teamStatistics) {
        this.teamStatistics = teamStatistics;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }
} 