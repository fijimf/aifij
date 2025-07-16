package com.fijimf.deepfij.model.quote;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "inspirational_quote")
public class InspirationalQuote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(name = "quote", nullable = false, columnDefinition = "TEXT")
    private String quote;

    @NotEmpty
    @Size(max = 255)
    @Column(name = "source", nullable = false)
    private String source;

    @Size(max = 100)
    @Column(name = "tag")
    private String tag;

    // Default constructor
    public InspirationalQuote() {
    }

    // Constructor with parameters
    public InspirationalQuote(String quote, String source, String tag) {
        this.quote = quote;
        this.source = source;
        this.tag = tag;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}