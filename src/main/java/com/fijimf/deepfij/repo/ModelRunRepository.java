package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.ml.ModelRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRunRepository extends JpaRepository<ModelRun, Long> {
    List<ModelRun> findByModelId(Long modelId);
    List<ModelRun> findByRunStatus(String runStatus);
}