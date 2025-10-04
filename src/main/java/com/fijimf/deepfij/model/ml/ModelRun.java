package com.fijimf.deepfij.model.ml;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "model_runs")
public class ModelRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @Column(name = "run_date", nullable = false)
    private LocalDateTime runDate;

    @Column(name = "run_status", nullable = false)
    private String runStatus;

    @Column(name = "run_result")
    private byte[] runResult;

    @OneToMany(mappedBy = "modelRun", fetch = FetchType.EAGER)
    private List<ModelRunParam> modelRunParams;

    @OneToMany(mappedBy = "modelRun", fetch = FetchType.EAGER)
    private List<ModelRunMetric> modelRunMetrics;

    public ModelRun() {
    }

    public ModelRun(Model model, LocalDateTime runDate, String runStatus) {
        this.model = model;
        this.runDate = runDate;
        this.runStatus = runStatus;
        this.modelRunParams = new ArrayList<>();
        this.modelRunMetrics = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public LocalDateTime getRunDate() {
        return runDate;
    }

    public void setRunDate(LocalDateTime runDate) {
        this.runDate = runDate;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public byte[] getRunResult() {
        return runResult;
    }

    public void setRunResult(byte[] runResult) {
        this.runResult = runResult;
    }

    public List<ModelRunParam> getModelRunParams() {
        return modelRunParams;
    }

    public void setModelRunParams(List<ModelRunParam> modelRunParams) {
        this.modelRunParams = modelRunParams;
    }

    public List<ModelRunMetric> getModelRunMetrics() {
        return modelRunMetrics;
    }

    public void setModelRunMetrics(List<ModelRunMetric> modelRunMetrics) {
        this.modelRunMetrics = modelRunMetrics;
    }
}