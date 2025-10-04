package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.ml.ModelRun;
import com.fijimf.deepfij.model.ml.ModelRunMetric;
import com.fijimf.deepfij.model.ml.ModelRunParam;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

public record ModelRunDTO(Long id, LocalDateTime date, String status, Map<String, String> params,
                          Map<String, String> metrics) {
    public static ModelRunDTO create(ModelRun r) {
        return new ModelRunDTO(r.getId(), r.getRunDate(), r.getRunStatus(), r.getModelRunParams().stream().collect(Collectors.toMap(ModelRunParam::getParamName, ModelRunParam::getParamValue)), r.getModelRunMetrics().stream().collect(Collectors.toMap(ModelRunMetric::getMetricName, ModelRunMetric::getMetricValue)));
    }
}
