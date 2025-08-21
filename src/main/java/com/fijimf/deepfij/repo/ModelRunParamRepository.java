package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.ml.ModelRunParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRunParamRepository extends JpaRepository<ModelRunParam, Long> {
    List<ModelRunParam> findByModelRunId(Long modelRunId);
    List<ModelRunParam> findByParamName(String paramName);
}