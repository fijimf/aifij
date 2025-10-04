package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.ml.Model;
import com.fijimf.deepfij.model.ml.ModelRun;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ModelRunRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ModelRunRepository modelRunRepository;
    
    @Autowired
    private ModelRepository modelRepository;

    @Test
    public void testInsertModelRun_HappyPath() {
        Model model = createDummyModel();
        model = modelRepository.save(model);
        
        ModelRun modelRun = createDummyModelRun(model);
        modelRun = modelRunRepository.save(modelRun);
        assertThat(modelRun.getId()).isGreaterThan(0L);
        assertThat(modelRunRepository.findById(modelRun.getId())).isPresent();
        assertThat(modelRunRepository.findAll()).hasSize(1);
    }

    @Test
    public void testBadInsert_NullModel() {
        ModelRun modelRun = createDummyModelRun(null);
        assertThatThrownBy(() -> {
            modelRunRepository.save(modelRun);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testBadInsert_NullRunDate() {
        Model model = createDummyModel();
        model = modelRepository.save(model);
        
        ModelRun modelRun = createDummyModelRun(model);
        modelRun.setRunDate(null);
        assertThatThrownBy(() -> {
            modelRunRepository.save(modelRun);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testBadInsert_NullRunStatus() {
        Model model = createDummyModel();
        model = modelRepository.save(model);
        
        ModelRun modelRun = createDummyModelRun(model);
        modelRun.setRunStatus(null);
        assertThatThrownBy(() -> {
            modelRunRepository.save(modelRun);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testFindByModelId() {
        Model model1 = createDummyModel(1);
        Model model2 = createDummyModel(2);
        model1 = modelRepository.save(model1);
        model2 = modelRepository.save(model2);

        ModelRun run1 = createDummyModelRun(model1);
        ModelRun run2 = createDummyModelRun(model1);
        ModelRun run3 = createDummyModelRun(model2);
        
        modelRunRepository.save(run1);
        modelRunRepository.save(run2);
        modelRunRepository.save(run3);

        List<ModelRun> model1Runs = modelRunRepository.findByModelId(model1.getId());
        assertThat(model1Runs).hasSize(2);
        Long model1Id = model1.getId();
        assertThat(model1Runs).allMatch(run -> run.getModel().getId().equals(model1Id));

        List<ModelRun> model2Runs = modelRunRepository.findByModelId(model2.getId());
        assertThat(model2Runs).hasSize(1);
        assertThat(model2Runs.get(0).getModel().getId()).isEqualTo(model2.getId());

        List<ModelRun> noRuns = modelRunRepository.findByModelId(999L);
        assertThat(noRuns).isEmpty();
    }

    @Test
    public void testFindByRunStatus() {
        Model model = createDummyModel();
        model = modelRepository.save(model);

        ModelRun run1 = createDummyModelRun(model, "STARTED");
        ModelRun run2 = createDummyModelRun(model, "COMPLETED");
        ModelRun run3 = createDummyModelRun(model, "STARTED");
        
        modelRunRepository.save(run1);
        modelRunRepository.save(run2);
        modelRunRepository.save(run3);

        List<ModelRun> startedRuns = modelRunRepository.findByRunStatus("STARTED");
        assertThat(startedRuns).hasSize(2);
        assertThat(startedRuns).allMatch(run -> run.getRunStatus().equals("STARTED"));

        List<ModelRun> completedRuns = modelRunRepository.findByRunStatus("COMPLETED");
        assertThat(completedRuns).hasSize(1);
        assertThat(completedRuns.get(0).getRunStatus()).isEqualTo("COMPLETED");

        List<ModelRun> failedRuns = modelRunRepository.findByRunStatus("FAILED");
        assertThat(failedRuns).isEmpty();
    }

    private static @NotNull Model createDummyModel() {
        return createDummyModel(0);
    }

    private static @NotNull Model createDummyModel(int version) {
        return new Model(
            "test-model" + version,
            "regression",
            "Test model description " + version,
            "com.example.TestModel" + version,
            true,
            false
        );
    }

    private static @NotNull ModelRun createDummyModelRun(Model model) {
        return createDummyModelRun(model, "STARTED");
    }

    private static @NotNull ModelRun createDummyModelRun(Model model, String status) {
        if (model == null) {
            ModelRun modelRun = new ModelRun();
            modelRun.setRunDate(LocalDateTime.now());
            modelRun.setRunStatus(status);
            return modelRun;
        }
        return new ModelRun(model, LocalDateTime.now(), status);
    }
}