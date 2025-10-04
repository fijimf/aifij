package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.ml.Model;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ModelRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ModelRepository modelRepository;

    @Test
    public void testInsertModel_HappyPath() {
        Model model = createDummyModel();
        model = modelRepository.save(model);
        assertThat(model.getId()).isGreaterThan(0L);
        assertThat(modelRepository.findById(model.getId())).isPresent();
        assertThat(modelRepository.findAll()).hasSize(1);
    }

    @Test
    public void testBadInsert_NullName() {
        Model model = createDummyModel();
        model.setName(null);
        assertThatThrownBy(() -> {
            modelRepository.save(model);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testBadInsert_NullType() {
        Model model = createDummyModel();
        model.setType(null);
        assertThatThrownBy(() -> {
            modelRepository.save(model);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testBadInsert_NullClassName() {
        Model model = createDummyModel();
        model.setClassName(null);
        assertThatThrownBy(() -> {
            modelRepository.save(model);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testBadInsert_NullFeaturesOk() {
        Model model = createDummyModel();
        model.setFeaturesOk(null);
        assertThatThrownBy(() -> {
            modelRepository.save(model);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testBadInsert_NullPipelineOk() {
        Model model = createDummyModel();
        model.setPipelineOk(null);
        assertThatThrownBy(() -> {
            modelRepository.save(model);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testBadInserts_DuplicateName() {
        Model model1 = createDummyModel();
        model1 = modelRepository.save(model1);

        assertThatThrownBy(() -> {
            Model model2 = createDummyModel();
            model2 = modelRepository.save(model2);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testFindByName() {
        Model model = createDummyModel();
        model = modelRepository.save(model);

        Optional<Model> found = modelRepository.findByName(model.getName());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(model.getId());

        Optional<Model> notFound = modelRepository.findByName("nonexistent");
        assertThat(notFound).isEmpty();
    }

    @Test
    public void testFindAllByName() {
        Model model1 = createDummyModel(1);
        Model model2 = createDummyModel(2);
        modelRepository.save(model1);
        modelRepository.save(model2);

        List<Model> found = modelRepository.findAllByName(model1.getName());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo(model1.getName());

        List<Model> notFound = modelRepository.findAllByName("nonexistent");
        assertThat(notFound).isEmpty();
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
}