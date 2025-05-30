package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.DeepFijApplication;
import com.fijimf.deepfij.model.schedule.Season;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = DeepFijApplication.class)
public class SeasonRepositoryTest {

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
    private SeasonRepository seasonRepository;

    @Test
    public void testInsertSeason() {
        // Create and save a season
        Season season = createDummySeason(2024, "2024-2025");
        season = seasonRepository.save(season);

        // Verify it was saved correctly
        assertThat(season.getId()).isNotNull();
        assertThat(seasonRepository.findById(season.getId())).isPresent();
        assertThat(seasonRepository.findAll()).hasSize(1);
    }

    @Test
    public void testFindByYear() {
        // Create and save multiple seasons
        Season season2024 = createDummySeason(2024, "2024-2025");
        seasonRepository.save(season2024);

        Season season2023 = createDummySeason(2023, "2023-2024");
        seasonRepository.save(season2023);

        // Test findByYear
        List<Season> results = seasonRepository.findByYear(2024);
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getYear()).isEqualTo(2024);
        assertThat(results.getFirst().getName()).isEqualTo("2024-2025");
    }

    @Test
    public void testFindFirstByOrderByYearDesc() {
        // Create and save multiple seasons with different years
        Season season2022 = createDummySeason(2022, "2022-2023");
        seasonRepository.save(season2022);

        Season season2023 = createDummySeason(2023, "2023-2024");
        seasonRepository.save(season2023);

        Season season2024 = createDummySeason(2024, "2024-2025");
        seasonRepository.save(season2024);

        // Test findFirstByOrderByYearDesc
        Season result = seasonRepository.findFirstByOrderByYearDesc();
        assertThat(result).isNotNull();
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getName()).isEqualTo("2024-2025");
    }

    @Test
    public void testUpdateSeason() {
        // Create and save a season
        Season season = createDummySeason(2024, "2024-2025");
        season = seasonRepository.save(season);
        Long id = season.getId();

        // Update the season
        LocalDate newStartDate = LocalDate.of(2024, 10, 15);
        LocalDate newEndDate = LocalDate.of(2025, 5, 15);
        season.setStartDate(newStartDate);
        season.setEndDate(newEndDate);
        seasonRepository.save(season);

        // Verify the update
        Season updated = seasonRepository.findById(id).orElseThrow();
        assertThat(updated.getStartDate()).isEqualTo(newStartDate);
        assertThat(updated.getEndDate()).isEqualTo(newEndDate);
    }

    @Test
    public void testDeleteSeason() {
        // Create and save a season
        Season season = createDummySeason(2024, "2024-2025");
        season = seasonRepository.save(season);
        Long id = season.getId();

        // Delete the season
        seasonRepository.delete(season);

        // Verify it was deleted
        assertThat(seasonRepository.findById(id)).isEmpty();
    }

    @Test
    public void testUniqueYearConstraint() {
        // Create and save a season
        Season season1 = createDummySeason(2024, "2024-2025");
        seasonRepository.save(season1);

        // Try to create another with the same year
        Season season2 = createDummySeason(2024, "2024-2025 Season");
        assertThatThrownBy(() -> {
            seasonRepository.save(season2);
            seasonRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testUniqueNameConstraint() {
        // Create and save a season
        Season season1 = createDummySeason(2024, "2024-2025");
        seasonRepository.save(season1);

        // Try to create another with the same name but different year
        Season season2 = createDummySeason(2023, "2024-2025");
        assertThatThrownBy(() -> {
            seasonRepository.save(season2);
            seasonRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testValidationConstraints() {
        // Test with null year
        Season season1 = createDummySeason(2024, "2024-2025");
        season1.setYear(null);
        assertThatThrownBy(() -> {
            seasonRepository.save(season1);
            seasonRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with null name
        Season season2 = createDummySeason(2024, null);
        assertThatThrownBy(() -> {
            seasonRepository.save(season2);
            seasonRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with empty name
        Season season3 = createDummySeason(2024, "");
        assertThatThrownBy(() -> {
            seasonRepository.save(season3);
            seasonRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with null start date
        Season season4 = createDummySeason(2024, "2024-2025");
        season4.setStartDate(null);
        assertThatThrownBy(() -> {
            seasonRepository.save(season4);
            seasonRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // Test with null end date
        Season season5 = createDummySeason(2024, "2024-2025");
        season5.setEndDate(null);
        assertThatThrownBy(() -> {
            seasonRepository.save(season5);
            seasonRepository.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    private Season createDummySeason(int year, String name) {
        Season season = new Season();
        season.setYear(year);
        season.setName(name);
        season.setStartDate(LocalDate.of(year, 11, 1));
        season.setEndDate(LocalDate.of(year + 1, 4, 30));
        return season;
    }
}