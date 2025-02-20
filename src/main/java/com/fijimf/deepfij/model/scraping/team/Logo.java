package com.fijimf.deepfij.model.scraping.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// Represents a Logo (Team logo images)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Logo {
    private String href;
    private String alt;
    private List<String> rel;
    private int width;
    private int height;

    // Getters and setters
    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public List<String> getRel() {
        return rel;
    }

    public void setRel(List<String> rel) {
        this.rel = rel;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
