package com.fijimf.deepfij.model.ml;

import jakarta.persistence.*;

@Entity
@Table(name = "model_run_params")
public class ModelRunParam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_run_id", nullable = false)
    private ModelRun modelRun;

    @Column(name = "param_name", nullable = false)
    private String paramName;

    @Column(name = "param_value", nullable = false)
    private String paramValue;

    public ModelRunParam() {}

    public ModelRunParam(ModelRun modelRun, String paramName, String paramValue) {
        this.modelRun = modelRun;
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ModelRun getModelRun() {
        return modelRun;
    }

    public void setModelRun(ModelRun modelRun) {
        this.modelRun = modelRun;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }
}