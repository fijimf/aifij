package com.fijimf.deepfij.model.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "team")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Size(max = 100)
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @NotEmpty
    @Size(max = 50)
    @Column(name = "nickname", nullable = false)
    private String nickname;


    @NotEmpty
    @Size(max = 5)
    @Column(name = "abbreviation", unique = true, nullable = false)
    private String abbreviation;

    @NotEmpty
    @Size(max = 150)
    @Column(name = "slug", unique = true, nullable = false)
    private String slug;

    @NotEmpty
    @Size(max = 150)
    @Column(name = "long_name", unique = true, nullable = false)
    private String longName;

    @NotEmpty
    @Size(max = 100)
    @Column(name = "espn_id", unique = true, nullable = false)
    private String espnId;

    @Pattern(regexp = "#[0-9A-Fa-f]{6}")
    @Column(name = "primary_color")
    private String primaryColor;

    @Pattern(regexp = "#[0-9A-Fa-f]{6}")
    @Column(name = "secondary_color")
    private String secondaryColor;

    @Size(max = 255)
    @Column(name = "logo_url")
    private String logoUrl;

    // Default constructor
    public Team() {
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getEspnId() {
        return espnId;
    }

    public void setEspnId(String espn_id) {
        this.espnId = espnId;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
} 