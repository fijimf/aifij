package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.DeepFijApplication;
import com.fijimf.deepfij.model.statistics.StatisticType;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = DeepFijApplication.class)
public class StatisticTypeRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = SharedPostgreSQLContainer.getInstance();

    @BeforeAll
    static void setup() {
        // Ensure the container is started before setting system properties
        postgreSQLContainer.start(); // Explicitly ensure it starts
        System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
        System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
    }

    @Autowired
    private StatisticTypeRepository statisticTypeRepository;

    @Test
    public void testInsertStatisticType() {
        // Create and save a statistic type
        StatisticType statisticType = createDummyStatisticType("Points Per Game", "PPG");
        statisticType = statisticTypeRepository.save(statisticType);

        // Verify it was saved correctly
        assertThat(statisticType.getId()).isNotNull();
        assertThat(statisticTypeRepository.findById(statisticType.getId())).isPresent();
        assertThat(statisticTypeRepository.findAll()).hasSize(1);
    }

    @Test
    public void testFindByCode() {
        // Create and save a statistic type
        StatisticType statisticType = createDummyStatisticType("Points Per Game", "PPG");
        statisticTypeRepository.save(statisticType);

        // Test findByCode
        Optional<StatisticType> result = statisticTypeRepository.findByCode("PPG");
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Points Per Game");
    }

    @Test
    public void testFindByName() {
        // Create and save a statistic type
        StatisticType statisticType = createDummyStatisticType("Points Per Game", "PPG");
        statisticTypeRepository.save(statisticType);

        // Test findByName
        Optional<StatisticType> result = statisticTypeRepository.findByName("Points Per Game");
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("PPG");
    }

    @Test
    public void testFindByCodeNotFound() {
        // Test findByCode with non-existent code
        Optional<StatisticType> result = statisticTypeRepository.findByCode("NONEXISTENT");
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByNameNotFound() {
        // Test findByName with non-existent name
        Optional<StatisticType> result = statisticTypeRepository.findByName("Nonexistent Statistic");
        assertThat(result).isEmpty();
    }

    @Test
    public void testUniqueCodeConstraint() {
        // Create and save a statistic type
        StatisticType statisticType1 = createDummyStatisticType("Points Per Game", "PPG");
        statisticTypeRepository.save(statisticType1);

        // Try to create another with the same code
        StatisticType statisticType2 = createDummyStatisticType("Different Name", "PPG");
        assertThatThrownBy(() -> {
            statisticTypeRepository.save(statisticType2);
            statisticTypeRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testUniqueNameConstraint() {
        // Create and save a statistic type
        StatisticType statisticType1 = createDummyStatisticType("Points Per Game", "PPG");
        statisticTypeRepository.save(statisticType1);

        // Try to create another with the same name
        StatisticType statisticType3 = createDummyStatisticType("Points Per Game", "DIF");
        assertThatThrownBy(() -> {
            statisticTypeRepository.save(statisticType3);
            statisticTypeRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testValidationConstraints() {
        // Test with null name
        StatisticType statisticType1 = createDummyStatisticType(null, "PPG");
        assertThatThrownBy(() -> {
            statisticTypeRepository.save(statisticType1);
            statisticTypeRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with empty name
        StatisticType statisticType2 = createDummyStatisticType("", "PPG");
        assertThatThrownBy(() -> {
            statisticTypeRepository.save(statisticType2);
            statisticTypeRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with null code
        StatisticType statisticType3 = createDummyStatisticType("Points Per Game", null);
        assertThatThrownBy(() -> {
            statisticTypeRepository.save(statisticType3);
            statisticTypeRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with empty code
        StatisticType statisticType4 = createDummyStatisticType("Points Per Game", "");
        assertThatThrownBy(() -> {
            statisticTypeRepository.save(statisticType4);
            statisticTypeRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void testUpdateStatisticType() {
        // Create and save a statistic type
        StatisticType statisticType = createDummyStatisticType("Points Per Game", "PPG");
        statisticType = statisticTypeRepository.save(statisticType);
        Long id = statisticType.getId();

        // Update the statistic type
        statisticType.setDescription("Updated description");
        statisticType.setIsHigherBetter(false);
        statisticType.setDecimalPlaces(3);
        statisticTypeRepository.save(statisticType);

        // Verify the update
        StatisticType updated = statisticTypeRepository.findById(id).orElseThrow();
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getIsHigherBetter()).isFalse();
        assertThat(updated.getDecimalPlaces()).isEqualTo(3);
    }

    @Test
    public void testDeleteStatisticType() {
        // Create and save a statistic type
        StatisticType statisticType = createDummyStatisticType("Points Per Game", "PPG");
        statisticType = statisticTypeRepository.save(statisticType);
        Long id = statisticType.getId();

        // Delete the statistic type
        statisticTypeRepository.delete(statisticType);

        // Verify it was deleted
        assertThat(statisticTypeRepository.findById(id)).isEmpty();
    }

    private StatisticType createDummyStatisticType(String name, String code) {
        StatisticType statisticType = new StatisticType();
        statisticType.setName(name);
        statisticType.setCode(code);
        statisticType.setDescription("Test statistic type");
        statisticType.setIsHigherBetter(true);
        statisticType.setDecimalPlaces(2);
        return statisticType;
    }
}
