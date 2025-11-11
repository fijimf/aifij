package com.fijimf.deepfij.service;

import com.fijimf.deepfij.model.dto.StatSummaryPage;
import com.fijimf.deepfij.model.schedule.Game;
import com.fijimf.deepfij.model.schedule.Season;
import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.model.statistics.TeamStatistic;
import com.fijimf.deepfij.repo.SeasonRepository;
import com.fijimf.deepfij.repo.StatisticTypeRepository;
import com.fijimf.deepfij.repo.TeamStatisticRepository;
import com.fijimf.deepfij.service.stat.LinearRegressionStatisticModel;
import com.fijimf.deepfij.service.stat.LogisticRegressionStatisticModel;
import com.fijimf.deepfij.service.stat.PointsStatisticModel;
import com.fijimf.deepfij.service.stat.WonLostStatisticModel;
import jakarta.annotation.PostConstruct;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticalService {
    public static final Logger logger = LoggerFactory.getLogger(StatisticalService.class);
    private final TeamStatisticRepository teamStatisticRepository;
    private final SeasonRepository seasonRepository;
    private final StatisticTypeRepository statisticTypeRepository;
    private final ApplicationContext applicationContext;
    private Map<String, StatisticalModel> statisticalModels;

    @Autowired
    public StatisticalService(@Autowired ApplicationContext applicationContext, @Autowired TeamStatisticRepository teamStatisticRepository, @Autowired SeasonRepository seasonRepository, @Autowired StatisticTypeRepository statisticTypeRepository, @Autowired WonLostStatisticModel wonLostStatisticModel, @Autowired PointsStatisticModel pointsStatisticModel, LinearRegressionStatisticModel linearRegressionStatisticModel, LogisticRegressionStatisticModel logisticRegressionStatisticModel) {
        this.teamStatisticRepository = teamStatisticRepository;
        this.seasonRepository = seasonRepository;
        this.statisticTypeRepository = statisticTypeRepository;
        this.applicationContext = applicationContext;
        statisticalModels = new HashMap<>();
    }

    @PostConstruct
    public void initializeModels() {
        Map<String, StatisticalModel> models = applicationContext.getBeansOfType(StatisticalModel.class);
        for (Map.Entry<String, StatisticalModel> fg : models.entrySet()) {
            fg.getValue().refreshDBTypes();
            statisticalModels.put(fg.getValue().key(), fg.getValue());
        }
    }


    @Transactional
    public List<TeamStatistic> generateStatistics(String yyyy, String modelKey) {
        Season season = seasonRepository.findByYear(Integer.parseInt(yyyy)).getFirst();
        if (season == null) {
            throw new IllegalArgumentException("No season found for year: " + yyyy);
        }
        StatisticalModel model = statisticalModels.get(modelKey);
        if (model == null) {
            throw new IllegalArgumentException("Invalid model: " + modelKey);
        }

        List<TeamStatistic> statistics = model.generate(season);
        logger.info("Saving " + statistics.size() + " stats for " + modelKey);

        // Use JDBC batch upsert for maximum performance
        teamStatisticRepository.batchUpsert(statistics);

        return statistics;
    }


    public List<String> modelKeys() {
        return statisticalModels.keySet().stream().sorted().toList();
    }

    public StatisticalService.StatisticsStatus getStatisticStatus() {
        List<Map<String, Object>> summary = teamStatisticRepository.findSummary();
        Map<String, Map<String, Map<String, List<TeamStatSeasonStatus>>>> modelMap = buildModelHierarchy(summary);
        statisticalModels.forEach((k, v) -> {
            if (!modelMap.containsKey(k)) {
                modelMap.put(k, new HashMap<>());
                v.refreshDBTypes().forEach(type -> {
                    modelMap.get(k).put(v.key(), new HashMap<>());
                });
            }
        });

        return createStatisticsStatus(modelMap);
    }

    private Map<String, Map<String, Map<String, List<TeamStatSeasonStatus>>>> buildModelHierarchy(List<Map<String, Object>> summary) {
        Map<String, Map<String, Map<String, List<TeamStatSeasonStatus>>>> model = new HashMap<>();

        summary.forEach(r -> {
            String modelKey = (String) r.get("model");
            String statKey = (String) r.get("stat");
            String seasonKey = Integer.toString((int) r.get("season"));

            TeamStatSeasonStatus status = new TeamStatSeasonStatus(
                    Integer.parseInt(seasonKey),
                    ((Long) r.get("num_days")).intValue(),
                    ((BigDecimal) r.get("total")).intValue(),
                    ((Date) r.get("last_date")).toLocalDate()
            );

            model.computeIfAbsent(modelKey, k -> new HashMap<>())
                    .computeIfAbsent(statKey, k -> new HashMap<>())
                    .computeIfAbsent(seasonKey, k -> new ArrayList<>())
                    .add(status);
        });

        return model;
    }

    private StatisticsStatus createStatisticsStatus(Map<String, Map<String, Map<String, List<TeamStatSeasonStatus>>>> models) {
        return StatisticsStatus.fromMap(models);
    }

    public record StatisticsStatus(
            List<ModelStatus> models
    ) {
        public static StatisticsStatus fromMap(Map<String, Map<String, Map<String, List<TeamStatSeasonStatus>>>> models) {
            return new StatisticsStatus(models.entrySet().stream().map(ModelStatus::fromEntry).toList());
        }
    }

    public record ModelStatus(
            String key,
            List<TeamStatisticStatus> teamStats
    ) {


        public static ModelStatus fromEntry(Map.Entry<String, Map<String, Map<String, List<TeamStatSeasonStatus>>>> entry) {
            return new ModelStatus(entry.getKey(), entry.getValue().entrySet().stream().map(TeamStatisticStatus::fromEntry).toList());
        }
    }

    public record TeamStatisticStatus(
            String key,
            List<TeamStatSeasonStatus> seasons
    ) {
        public static TeamStatisticStatus fromEntry(Map.Entry<String, Map<String, List<TeamStatSeasonStatus>>> entry) {
            return new TeamStatisticStatus(
                    entry.getKey(),
                    entry.getValue().entrySet().stream().flatMap(e -> e.getValue().stream()).toList()
            );
        }
    }

    public record TeamStatSeasonStatus(
            int year,
            int numDates,
            int numStats,
            LocalDate lastDate
    ) {
    }

    // Methods migrated from StatisticServiceImpl

    /**
     * Retrieves the top N teams for a given date, season, and statistic type.
     * The ordering is determined by the isHigherBetter flag in the StatisticType.
     *
     * @param seasonId          The ID of the season
     * @param statisticTypeName The name of the statistic type
     * @param date              The date to get statistics for
     * @param limit             The maximum number of teams to return (0 for all teams)
     * @return List of TeamStatistic objects ordered by value
     */
    public List<TeamStatistic> getTopTeamsByDate(Long seasonId, String statisticTypeName, LocalDate date, int limit) {
        StatisticType statisticType = statisticTypeRepository.findByName(statisticTypeName)
                .orElseThrow(() -> new IllegalArgumentException("Statistic type not found: " + statisticTypeName));

        List<TeamStatistic> statistics = teamStatisticRepository.findBySeasonIdAndStatisticTypeIdAndStatisticDate(
                seasonId, statisticType.getId(), date);

        List<TeamStatistic> teamStatisticList = statistics.stream()
                .filter(stat -> stat.getNumericValue() != null)
                .sorted((a, b) -> {
                    int comparison = a.getNumericValue().compareTo(b.getNumericValue());
                    return Boolean.TRUE.equals(statisticType.getIsHigherBetter()) ?
                            -comparison : // Higher is better, so reverse the comparison
                            comparison;   // Lower is better, so keep the comparison
                })
                .collect(Collectors.toList());
        return limit <= 0 ? teamStatisticList : teamStatisticList.subList(0, limit);
    }

    /**
     * Gets a statistics summary page for a given statistic type and season.
     * Uses the most recent completed game date.
     *
     * @param statisticTypeName The name of the statistic type
     * @param season            The season
     * @return StatSummaryPage with statistics and summary data
     */
    public StatSummaryPage getStatSummaryPage(String statisticTypeName, Season season) {
        List<LocalDate> dates = season.getGames().stream().filter(Game::isComplete).map(Game::getDate).toList();
        if (dates.isEmpty()) {
            int yyyy = seasonRepository.findPreviousSeason(season.getYear());
            return getStatSummaryPage(statisticTypeName, seasonRepository.findByYear(yyyy).getFirst(), LocalDate.now());
        } else {
            LocalDate last = dates.getLast();
            return getStatSummaryPage(statisticTypeName, season, last);
        }
    }

    /**
     * Gets a statistics summary page for a given statistic type, season, and specific date.
     *
     * @param statisticTypeName The name of the statistic type
     * @param season            The season
     * @param date              The specific date
     * @return StatSummaryPage with statistics and summary data
     */
    public StatSummaryPage getStatSummaryPage(String statisticTypeName, Season season, LocalDate date) {
        StatisticType statisticType = statisticTypeRepository.findByName(statisticTypeName)
                .orElseThrow(() -> new IllegalArgumentException("Statistic type not found: " + statisticTypeName));
        Optional<LocalDate> lastCompleteDate = season.getGames()
                .stream()
                .filter(Game::isComplete)
                .map(Game::getDate)
                .max(LocalDate::compareTo);
        if (lastCompleteDate.isPresent()) {
            List<TeamStatistic> teamStatistics = getTopTeamsByDate(season.getId(), statisticTypeName, lastCompleteDate.get(), 0);
            SortedMap<LocalDate, DescriptiveStatistics> descriptiveStatsByDate = getDescriptiveTimeSeries(statisticTypeName, season);
            return StatSummaryPage.create(season, date, statisticType, teamStatistics, descriptiveStatsByDate);
        } else {
            throw new IllegalArgumentException("No completed games found for season: " + season.getYear());
        }
    }

    /**
     * Gets descriptive statistics time series for a statistic type and season.
     *
     * @param statisticTypeName The name of the statistic type
     * @param season            The season
     * @return Sorted map of dates to descriptive statistics
     */
    private SortedMap<LocalDate, DescriptiveStatistics> getDescriptiveTimeSeries(String statisticTypeName, Season season) {
        StatisticType statisticType = statisticTypeRepository.findByName(statisticTypeName)
                .orElseThrow(() -> new IllegalArgumentException("Statistic type not found: " + statisticTypeName));
        List<TeamStatistic> statistics = teamStatisticRepository.findBySeasonIdAndStatisticTypeId(
                season.getId(), statisticType.getId());
        Map<LocalDate, List<TeamStatistic>> statsByDate = statistics.stream()
                .collect(Collectors.groupingBy(TeamStatistic::getStatisticDate));
        SortedMap<LocalDate, DescriptiveStatistics> data = new TreeMap<>();
        statsByDate.forEach((k, v) -> {
            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
            v.stream().filter(s -> s.getNumericValue() != null).forEach(t -> descriptiveStatistics.addValue(t.getNumericValue().doubleValue()));
            data.put(k, descriptiveStatistics);
        });
        return data;
    }

    /**
     * Gets list of all available statistic model codes.
     *
     * @return List of statistic type codes
     */
    public List<String> getModelStats() {
        return statisticTypeRepository.findAll().stream().map(StatisticType::getCode).toList();
    }
}
