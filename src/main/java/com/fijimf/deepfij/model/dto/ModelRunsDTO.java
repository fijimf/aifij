package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.ml.Model;
import com.fijimf.deepfij.model.ml.ModelRun;

import java.util.List;

public record ModelRunsDTO(Model model, List<ModelRunDTO> modelRuns) {
    public static ModelRunsDTO create(Model model, List<ModelRun> runs) {
        return new ModelRunsDTO(model, runs.stream().map(ModelRunDTO::create).toList());
    }
}

