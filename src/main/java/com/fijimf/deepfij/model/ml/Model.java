package com.fijimf.deepfij.model.ml;

import jakarta.persistence.*;

@Entity
@Table(name = "models")
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(length = 1023)
    private String description;

    @Column(name = "class_name", nullable = false, length = 511)
    private String className;

    private byte[] pipeline;

    @Column(name = "features_ok", nullable = false)
    private Boolean featuresOk;

    @Column(name = "pipeline_ok", nullable = false)
    private Boolean pipelineOk;

    public Model() {}

    public Model(String name, String type, String description, String className, 
                 Boolean featuresOk, Boolean pipelineOk) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.className = className;
        this.featuresOk = featuresOk;
        this.pipelineOk = pipelineOk;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public byte[] getPipeline() {
        return pipeline;
    }

    public void setPipeline(byte[] pipeline) {
        this.pipeline = pipeline;
    }

    public Boolean getFeaturesOk() {
        return featuresOk;
    }

    public void setFeaturesOk(Boolean featuresOk) {
        this.featuresOk = featuresOk;
    }

    public Boolean getPipelineOk() {
        return pipelineOk;
    }

    public void setPipelineOk(Boolean pipelineOk) {
        this.pipelineOk = pipelineOk;
    }
}