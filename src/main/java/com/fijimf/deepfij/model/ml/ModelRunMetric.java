package com.fijimf.deepfij.model.ml;

import jakarta.persistence.*;

@Entity
@Table(name = "model_run_metrics")
public class ModelRunMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_run_id", nullable = false)
    private ModelRun modelRun;

    @Column(name = "metric_name")
    private String metricName;

    @Column(name = "metric_value")
    private String metricValue;

    public ModelRunMetric() {}

    public ModelRunMetric(ModelRun modelRun, String metricName, String metricValue) {
        this.modelRun = modelRun;
        this.metricName = metricName;
        this.metricValue = metricValue;
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

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(String metricValue) {
        this.metricValue = metricValue;
    }
}