package com.fijimf.deepfij.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fijimf.deepfij.model.statistics.StatisticSummary;
import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repo.StatisticTypeRepository;
import com.fijimf.deepfij.repo.TeamStatisticRepository;

@ExtendWith(MockitoExtension.class)
class StatisticServiceImplTest {

    @Mock
    private TeamStatisticRepository teamStatisticRepository;

    @Mock
    private StatisticTypeRepository statisticTypeRepository;

    @InjectMocks
    private StatisticServiceImpl statisticService;

    private StatisticType testStatisticType;
    private LocalDate testDate;
    private List<TeamStatistic> testStatistics;

    @BeforeEach
    void setUp() {
        testStatisticType = new StatisticType();
        testStatisticType.setId(1L);
        testStatisticType.setName("testStat");

        testDate = LocalDate.of(2024, 1, 1);

        // Create test data with values: 1, 2, 3, 4, 5
        testStatistics = Arrays.asList(
            createTeamStatistic(1L, testDate, new BigDecimal("1.0")),
            createTeamStatistic(2L, testDate, new BigDecimal("2.0")),
            createTeamStatistic(3L, testDate, new BigDecimal("3.0")),
            createTeamStatistic(4L, testDate, new BigDecimal("4.0")),
            createTeamStatistic(5L, testDate, new BigDecimal("5.0"))
        );
    }

    @Test
    void getStatisticSummariesBySeasonAndType_ShouldCalculateCorrectStatistics() {
        // Arrange
        when(statisticTypeRepository.findByName("testStat"))
            .thenReturn(Optional.of(testStatisticType));
        when(teamStatisticRepository.findBySeasonIdAndStatisticTypeId(1L, 1L))
            .thenReturn(testStatistics);

        // Act
        List<StatisticSummary> result = statisticService.getStatisticSummariesBySeasonAndType(1L, "testStat");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        StatisticSummary summary = result.get(0);
        assertEquals(testDate, summary.date());
        assertEquals(5, summary.count());
        assertEquals(new BigDecimal("1.0"), summary.minimum());
        assertEquals(new BigDecimal("2.0"), summary.firstQuartile());
        assertEquals(new BigDecimal("3.0"), summary.median());
        assertEquals(new BigDecimal("4.0"), summary.thirdQuartile());
        assertEquals(new BigDecimal("5.0"), summary.maximum());
        assertEquals(new BigDecimal("3.0000"), summary.mean());
        assertEquals(new BigDecimal("1.4142"), summary.standardDeviation());
    }

    @Test
    void getStatisticSummariesBySeasonAndType_ShouldHandleMultipleDates() {
        // Arrange
        LocalDate secondDate = testDate.plusDays(1);
        List<TeamStatistic> allStatistics = Arrays.asList(
            createTeamStatistic(1L, testDate, new BigDecimal("1.0")),
            createTeamStatistic(2L, testDate, new BigDecimal("2.0")),
            createTeamStatistic(3L, secondDate, new BigDecimal("3.0")),
            createTeamStatistic(4L, secondDate, new BigDecimal("4.0"))
        );

        when(statisticTypeRepository.findByName("testStat"))
            .thenReturn(Optional.of(testStatisticType));
        when(teamStatisticRepository.findBySeasonIdAndStatisticTypeId(1L, 1L))
            .thenReturn(allStatistics);

        // Act
        List<StatisticSummary> result = statisticService.getStatisticSummariesBySeasonAndType(1L, "testStat");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testDate, result.get(0).date());
        assertEquals(secondDate, result.get(1).date());
    }

    @Test
    void getStatisticSummariesBySeasonAndType_ShouldThrowExceptionForInvalidStatisticType() {
        // Arrange
        when(statisticTypeRepository.findByName("invalidStat"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            statisticService.getStatisticSummariesBySeasonAndType(1L, "invalidStat")
        );
    }

    @Test
    void getStatisticSummariesBySeasonAndType_ShouldHandleNullValues() {
        // Arrange
        List<TeamStatistic> statisticsWithNull = Arrays.asList(
            createTeamStatistic(1L, testDate, new BigDecimal("1.0")),
            createTeamStatistic(2L, testDate, null),
            createTeamStatistic(3L, testDate, new BigDecimal("3.0"))
        );

        when(statisticTypeRepository.findByName("testStat"))
            .thenReturn(Optional.of(testStatisticType));
        when(teamStatisticRepository.findBySeasonIdAndStatisticTypeId(1L, 1L))
            .thenReturn(statisticsWithNull);

        // Act
        List<StatisticSummary> result = statisticService.getStatisticSummariesBySeasonAndType(1L, "testStat");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).count()); // Should only count non-null values
    }

    @Test
    void getTopTeamsByDate_ShouldReturnTopTeamsWhenHigherIsBetter() {
        // Arrange
        testStatisticType.setIsHigherBetter(true);
        when(statisticTypeRepository.findByName("testStat"))
            .thenReturn(Optional.of(testStatisticType));
        when(teamStatisticRepository.findBySeasonIdAndStatisticTypeIdAndStatisticDate(1L, 1L, testDate))
            .thenReturn(testStatistics);

        // Act
        List<TeamStatistic> result = statisticService.getTopTeamsByDate(1L, "testStat", testDate, 3);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(new BigDecimal("5.0"), result.get(0).getNumericValue());
        assertEquals(new BigDecimal("4.0"), result.get(1).getNumericValue());
        assertEquals(new BigDecimal("3.0"), result.get(2).getNumericValue());
    }

    @Test
    void getTopTeamsByDate_ShouldReturnTopTeamsWhenLowerIsBetter() {
        // Arrange
        testStatisticType.setIsHigherBetter(false);
        when(statisticTypeRepository.findByName("testStat"))
            .thenReturn(Optional.of(testStatisticType));
        when(teamStatisticRepository.findBySeasonIdAndStatisticTypeIdAndStatisticDate(1L, 1L, testDate))
            .thenReturn(testStatistics);

        // Act
        List<TeamStatistic> result = statisticService.getTopTeamsByDate(1L, "testStat", testDate, 3);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(new BigDecimal("1.0"), result.get(0).getNumericValue());
        assertEquals(new BigDecimal("2.0"), result.get(1).getNumericValue());
        assertEquals(new BigDecimal("3.0"), result.get(2).getNumericValue());
    }

    @Test
    void getTopTeamsByDate_ShouldHandleNullValues() {
        // Arrange
        testStatisticType.setIsHigherBetter(true);
        List<TeamStatistic> statisticsWithNull = Arrays.asList(
            createTeamStatistic(1L, testDate, new BigDecimal("1.0")),
            createTeamStatistic(2L, testDate, null),
            createTeamStatistic(3L, testDate, new BigDecimal("3.0"))
        );

        when(statisticTypeRepository.findByName("testStat"))
            .thenReturn(Optional.of(testStatisticType));
        when(teamStatisticRepository.findBySeasonIdAndStatisticTypeIdAndStatisticDate(1L, 1L, testDate))
            .thenReturn(statisticsWithNull);

        // Act
        List<TeamStatistic> result = statisticService.getTopTeamsByDate(1L, "testStat", testDate, 2);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("3.0"), result.get(0).getNumericValue());
        assertEquals(new BigDecimal("1.0"), result.get(1).getNumericValue());
    }

    @Test
    void getTopTeamsByDate_ShouldThrowExceptionForInvalidStatisticType() {
        // Arrange
        when(statisticTypeRepository.findByName("invalidStat"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            statisticService.getTopTeamsByDate(1L, "invalidStat", testDate, 5)
        );
    }

    private TeamStatistic createTeamStatistic(Long id, LocalDate date, BigDecimal value) {
        TeamStatistic stat = new TeamStatistic();
        stat.setId(id);
        stat.setStatisticDate(date);
        stat.setStatisticType(testStatisticType);
        stat.setNumericValue(value);
        return stat;
    }
} 