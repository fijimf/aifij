package com.fijimf.deepfij.model.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "conference")
public class Conference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Size(max = 100)
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @NotEmpty
    @Size(max = 50)
    @Column(name = "short_name", unique = true, nullable = false)
    private String shortName;

    @Size(max = 255)
    @Column(name = "logo_url")
    private String logoUrl;

    @Size(max = 50)
    @Column(name = "espn_id", nullable = false, unique = true)
    private String espnId;

    // Default constructor
    public Conference() {
    }

    // Getters and setters
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getEspnId() {
        return espnId;
    }

    public void setEspnId(String espnId) {
        this.espnId = espnId;
    }
} 