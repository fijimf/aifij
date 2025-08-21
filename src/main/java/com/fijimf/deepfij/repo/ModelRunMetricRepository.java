package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.ml.ModelRunMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRunMetricRepository extends JpaRepository<ModelRunMetric, Long> {
    List<ModelRunMetric> findByModelRunId(Long modelRunId);
    List<ModelRunMetric> findByMetricName(String metricName);
}