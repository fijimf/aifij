package com.fijimf.deepfij.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fijimf.deepfij.model.User;
import com.fijimf.deepfij.model.schedule.*;
import com.fijimf.deepfij.model.scraping.conference.RawConference;
import com.fijimf.deepfij.model.scraping.scoreboard.ScoreboardResponse;
import com.fijimf.deepfij.model.scraping.standings.ConferenceStanding;
import com.fijimf.deepfij.model.scraping.standings.StandingsEntry;
import com.fijimf.deepfij.model.scraping.standings.StandingsResponse;
import com.fijimf.deepfij.model.scraping.team.RawTeam;
import com.fijimf.deepfij.repo.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ScheduleService {

    private final ScrapingService scrapingService;
    private final TeamRepository teamRepository;
    private final ConferenceRepository conferenceRepository;
    private final ConferenceMappingRepository conferenceMappingRepository;
    private final GameRepository gameRepository;
    private final SeasonRepository seasonRepository;
    private final AuditRepository auditRepository;

    @PersistenceContext
    private final EntityManager entityManager; // Direct access to handle flushing
    private final TeamStatisticRepository teamStatisticRepository;

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired
    public ScheduleService(ScrapingService scrapingService, TeamRepository teamRepository, ConferenceRepository conferenceRepository, ConferenceMappingRepository conferenceMappingRepository, GameRepository gameRepository, SeasonRepository seasonRepository, AuditRepository auditRepository, EntityManager entityManager, TeamStatisticRepository teamStatisticRepository) {
        this.scrapingService = scrapingService;
        this.teamRepository = teamRepository;
        this.conferenceRepository = conferenceRepository;
        this.conferenceMappingRepository = conferenceMappingRepository;
        this.gameRepository = gameRepository;
        this.seasonRepository = seasonRepository;
        this.auditRepository = auditRepository;
        this.entityManager = entityManager;
        this.teamStatisticRepository = teamStatisticRepository;
    }

    public List<Conference> loadConferences(User user) {
        LocalDateTime start = LocalDateTime.now();
        scrapingService.fetchConferences()
                .stream()
                .filter(RawConference::isConference)
                .forEach(c -> {
                    List<Conference> conferenceList = conferenceRepository.findByEspnId(c.groupId());
                    if (conferenceList.isEmpty()) {
                        conferenceRepository.save(c.toConference());
                        logger.info("Conference " + c.name() + " inserted from ESPN");
                    } else {
                        Conference conference = conferenceList.getFirst();
                        c.updateConference(conference);
                        conferenceRepository.save(conference);
                        logger.info("Conference " + conference.getName() + " updated from ESPN");
                    }
                });
        LocalDateTime end = LocalDateTime.now();
        List<Conference> conferences = conferenceRepository.findAll();
        auditRepository.save(new Audit(0L, "loadConferences", "%d conferences loaded".formatted(conferences.size()), Timestamp.valueOf(start), Timestamp.valueOf(end), user));
        return conferences;
    }

    public int dropConferences(User user) {
        LocalDateTime start = LocalDateTime.now();
        long count = conferenceRepository.count();
        conferenceRepository.deleteAll();
        LocalDateTime end = LocalDateTime.now();
        auditRepository.save(new Audit(0L, "dropConferences", "%d conferences dropped".formatted(conferenceRepository.count()), Timestamp.valueOf(start), Timestamp.valueOf(end), user));
        return (int) count;
    }

    public List<Team> loadTeams(User user) {
        LocalDateTime start = LocalDateTime.now();
        scrapingService.fetchTeams()
                .getFirst()
                .leagues()
                .getFirst()
                .teams()
                .forEach(w -> {
                    List<Team> teams = teamRepository.findByEspnId(w.rawTeam().id());
                    if (teams.isEmpty()) {
                        teamRepository.save(w.rawTeam().getTeam());
                        logger.info("Team " + w.rawTeam().name() + " inserted from ESPN");
                    } else {
                        Team t = teams.getFirst();
                        w.updateTeam(t);
                        teamRepository.save(t);
                        logger.info("Team " + t.getName() + " updated from ESPN");
                    }
                });
        LocalDateTime end = LocalDateTime.now();
        List<Team> teams = teamRepository.findAll();
        auditRepository.save(new Audit(0L, "loadTeams", "%d teams loaded".formatted(teams.size()), Timestamp.valueOf(start), Timestamp.valueOf(end), user));
        return teams;
    }

    public int dropTeams(User user) {
        LocalDateTime start = LocalDateTime.now();
        long count = teamRepository.count();
        teamRepository.deleteAll();
        LocalDateTime end = LocalDateTime.now();
        auditRepository.save(new Audit(0L, "dropTeams", "%d teams dropped".formatted(count), Timestamp.valueOf(start), Timestamp.valueOf(end), user));
        return (int) count;
    }

    public List<Game> fetchGames(LocalDate index, Season season, User user) {
        LocalDateTime start = LocalDateTime.now();
        logger.info("Fetching games for " + index);
        ScoreboardResponse scoreboard = scrapingService.fetchScoreboard(Integer.parseInt(index.format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
        if (scoreboard == null) {
            logger.error("Scoreboard for index " + index.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + " is null");
            return Collections.emptyList();
        }

        Map<String, Team> teams = conferenceMappingRepository
                .findBySeason(season).stream()
                .map(ConferenceMapping::getTeam)
                .collect(Collectors.toMap(Team::getEspnId, Function.identity()));
        logger.info("Scraped " + scoreboard.events().size() + " events for date " + index);

        List<Game> games = scoreboard.events().stream().flatMap(event -> {
            Game g = new Game();
            g.setSeason(season);
            g.setDate(event.date()
                    .withZoneSameInstant(ZoneId.of("America/New_York"))
                    .toLocalDate());
            g.setTime(event.date()
                    .withZoneSameInstant(ZoneId.of("America/New_York"))
                    .toLocalTime());
            g.setIndexDate(index);
            g.setEspnId(event.id());
            g.setLocation(event.location());
            g.setNeutralSite(event.neutralSite());

            g.setPeriods(event.fullStatus().period());
            event.competitors().forEach(competitor -> {
                if (competitor.homeAway().equals("home")) {
                    if (teams.containsKey(competitor.id())) {
                        Team t = teams.get(competitor.id());
                        g.setHomeTeam(t);
                        if (competitor.score() != null && !competitor.score().isBlank()) {
                            g.setHomeScore(Integer.valueOf(competitor.score()));
                        }
                        if (competitor.tournamentMatchup() != null) {
                            g.setHomeTeamSeed(competitor.tournamentMatchup().seed());
                        }
                    } else {
                        logger.error("Team " + competitor.name() + " not found in database");
                    }
                } else if (competitor.homeAway().equals("away")) {
                    if (teams.containsKey(competitor.id())) {
                        Team t = teams.get(competitor.id());
                        g.setAwayTeam(t);
                        if (competitor.score() != null && !competitor.score().isBlank()) {
                            g.setAwayScore(Integer.valueOf(competitor.score()));
                        }
                        if (competitor.tournamentMatchup() != null) {
                            g.setAwayTeamSeed(competitor.tournamentMatchup().seed());
                        }
                    } else {
                        logger.error("Team " + competitor.name() + " not found in database");
                    }
                }
            });
            if (event.odds() != null) {
                g.setOverUnder(event.odds().overUnder());
                g.setSpread(event.odds().spread());
                if (event.odds().away() != null) {
                    g.setAwayMoneyLine(event.odds().away().moneyLine());
                }
                if (event.odds().home() != null) {
                    g.setHomeMoneyLine(event.odds().home().moneyLine());
                }
            }
            g.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
            if (g.getHomeTeam() != null && g.getAwayTeam() != null) {
                return Stream.of(g);
            } else {
                logger.error("Game " + event.name() + " has missing teams.");
                return Stream.empty();
            }
        }).toList();

        logger.info("Converted " + games.size() + " games for date " + index);
        LocalDateTime end = LocalDateTime.now();
        auditRepository.save(new Audit(0L, "fetchGames", "%d games fetched".formatted(games.size()), Timestamp.valueOf(start), Timestamp.valueOf(end), user));
        return games;

    }

    private Season findOrCreate(int yyyy, User user) {
        List<Season> seasonList = seasonRepository.findByYear(yyyy);
        if (!seasonList.isEmpty()) {
            return seasonList.getFirst();
        } else {
            LocalDateTime start = LocalDateTime.now();
            Season season = new Season();
            season.setId(0L);
            season.setName((yyyy - 1) + "-" + yyyy % 100);
            season.setStartDate(LocalDate.of(yyyy - 1, 11, 1));
            season.setEndDate(LocalDate.of(yyyy, 4, 30));
            season.setYear(yyyy);
            Season saved = seasonRepository.save(season);
            LocalDateTime end = LocalDateTime.now();
            auditRepository.save(new Audit(0L, "findOrCreate", "%d seasons loaded".formatted(seasonList.size()), Timestamp.valueOf(start), Timestamp.valueOf(end), user));
            return saved;
        }
    }

    @Transactional
    public void createSchedule(int yyyy, User user) {

        if (teamRepository.count() == 0) {
            loadTeams(user);
        }
        if (conferenceRepository.count() == 0) {
            loadConferences(user);
        }

        Season s = findOrCreate(yyyy, user);
        long mappingsDeleted = deleteMappings(s, user);

        logger.info("Deleted " + mappingsDeleted + " mappings for season " + s.getName());
        createConferenceMappings(yyyy, s, user);

        logger.info("For " + yyyy + " there are " + conferenceMappingRepository.count() + " teams");
        LocalDateTime start = LocalDateTime.now();
        Stream.iterate(s.getStartDate(), date -> !date.isAfter(s.getEndDate()), date -> date.plusDays(1)).forEach(
                d -> {
                    List<Game> games = fetchGames(d, s, user);

                    updateGames(d, s, games);
                }
        );
        LocalDateTime end = LocalDateTime.now();
        auditRepository.save(new Audit(0L, "createSchedule", "%d games fetched".formatted(gameRepository.count()), Timestamp.valueOf(start), Timestamp.valueOf(end), user));
    }

    private void createConferenceMappings(int yyyy, Season s, User user) {
        LocalDateTime start = LocalDateTime.now();
        StandingsResponse standingsResponse = scrapingService.fetchStandings(yyyy);
        standingsResponse.children().forEach(cs -> {
            Conference c = findOrCreateConference(cs);
            cs.consolidatedStandings().forEach(se -> {
                Team t = findOrCreateTeam(se);
                ConferenceMapping mapping = new ConferenceMapping();
                mapping.setSeason(s);
                mapping.setConference(c);
                mapping.setTeam(t);
                logger.info("Mapping team " + t.getName() + " to conference " + c.getName() + " in season " + s.getName());
                conferenceMappingRepository.save(mapping);
            });
        });
        LocalDateTime end = LocalDateTime.now();
        auditRepository.save(new Audit(0L, "createConferenceMappings", "%d standings fetched".formatted(standingsResponse.children().size()), Timestamp.valueOf(start), Timestamp.valueOf(end), user));
    }

    private long deleteMappings(Season s, User user) {
        LocalDateTime start = LocalDateTime.now();
        long mappingsDeleted = conferenceMappingRepository.deleteBySeason(s);
        LocalDateTime end = LocalDateTime.now();
        auditRepository.save(new Audit(0L, "deleteMappings", "%d mappings deleted".formatted(mappingsDeleted), Timestamp.valueOf(start), Timestamp.valueOf(end), user));
        entityManager.flush();
        return mappingsDeleted;
    }

    private void updateGames(LocalDate index, Season s, List<Game> games) {
        logger.info("For index {} {} were scraped", index, games.size());
        Map<String, Game> oldGames = gameRepository.findBySeasonAndIndexDate(s, index).stream().collect(Collectors.toMap(Game::getEspnId, Function.identity()));
        logger.info("For index {} {} were in the DB", index, oldGames.size());

        Map<Boolean, List<Game>> partitionedGames = games
                .stream()
                .collect(Collectors.partitioningBy(g -> oldGames.containsKey(g.getEspnId())));
        List<Game> inserts = partitionedGames.get(false);
        logger.info("For date " + index + " " + inserts.size() + " were new");
        List<Game> updates = partitionedGames.get(true);
        gameRepository.saveAll(inserts);
        logger.info("For date " + index + " " + updates.size() + " were potentially updated.");

        List<Game> updatedGames = updates.stream().map(g -> Game.update(oldGames.get(g.getEspnId()), g)).filter(Objects::nonNull).toList();
        logger.info("For date " + index + " " + updatedGames.size() + " were actually updated");
        gameRepository.saveAll(updatedGames);

        Set<String> ids = games.stream().map(Game::getEspnId).collect(Collectors.toSet());
        List<Game> deleteGames = oldGames.entrySet().stream().filter(e -> !ids.contains(e.getKey())).map(Map.Entry::getValue).toList();
        gameRepository.deleteAll(deleteGames);
        logger.info("For date " + index + " " + deleteGames.size() + " were deleted");
        entityManager.flush();

        logger.info("For index " + index + " there are " + gameRepository.findBySeasonAndIndexDate(s, index).size() + " games");
    }

    private Team findOrCreateTeam(StandingsEntry se) {
        List<Team> teams = teamRepository.findByEspnId(se.rawTeam().id());
        if (!teams.isEmpty()) {
            return teams.getFirst();
        } else {
            logger.info("Team " + se.rawTeam().name() + " is not a known team.  Retrieving by ID");
            RawTeam rawTeam = scrapingService.fetchTeamById(se.rawTeam().id());
            if (rawTeam != null) {
                return teamRepository.save(rawTeam.getTeam());
            }
            ObjectMapper mapper = new ObjectMapper();
            try {
                logger.info(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(se.rawTeam()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return teamRepository.save(se.rawTeam().getTeam());
        }
    }

    private Conference findOrCreateConference(ConferenceStanding cs) {
        List<Conference> confs = conferenceRepository.findByEspnId(cs.id());
        if (!confs.isEmpty()) {
            return confs.getFirst();
        } else {
            logger.info("Conference Standings " + cs.name() + "," + cs.id() + " is not a known conference.  Creating conference from standings.");
            Conference c = new Conference();
            c.setName(cs.name());
            c.setEspnId(cs.id());
            c.setLogoUrl(null);
            c.setShortName(cs.shortName());
            return conferenceRepository.save(c);
        }
    }

    public ScheduleStatus getStatus() {
        List<SeasonStatus> seasons = seasonRepository.findAll().stream().map(s -> {
            int year = s.getYear();
            List<ConferenceMapping> mappings = conferenceMappingRepository.findBySeason(s);
            long seasonTeams = mappings.size();
            long seasonConferences = mappings.stream().map(ConferenceMapping::getConference).distinct().count();
            List<Game> games = gameRepository.findBySeasonOrderByDateAsc(s);
            long seasonGames = games.size();
            LocalDate first = games.getFirst().getDate();
            LocalDate last = games.getLast().getDate();
            LocalDate lastComplete = games.stream().filter(Game::isComplete).map(Game::getDate).toList().getLast();
            Timestamp lastUpdated = games.stream().map(Game::getUpdatedAt).max(Comparator.naturalOrder()).orElse(null);
            return new SeasonStatus(year, seasonTeams, seasonConferences, seasonGames, first, last, lastComplete, lastUpdated);
        }).toList();
        return new ScheduleStatus(getTeamStatus(), getConferenceStatus(), seasons);
    }

    public ConferenceStatus getConferenceStatus() {
        long numConferences = conferenceRepository.count();
        if (numConferences == 0) {
            return new ConferenceStatus(0, "No conferences.");
        } else {
            long missingLogo = conferenceRepository.countByLogoUrlIsNull();
            return new ConferenceStatus(numConferences, missingLogo > 0 ? "Missing logo for " + missingLogo + " conferences." : "OK");
        }
    }

    public TeamStatus getTeamStatus() {
        long numTeams = teamRepository.count();
        if (numTeams == 0) {
            return new TeamStatus(0, "No teams.");
        } else {
            long missingLogo = teamRepository.countByLogoUrlIsNull();
            long missingColor = teamRepository.countByPrimaryColorIsNull();
            String badData = (missingColor > 0 ? "Missing color for " + missingColor + " teams.  " : "") +
                    (missingLogo > 0 ? "Missing logo for " + missingLogo + " teams." : "");
            return new TeamStatus(numTeams, StringUtils.isNotBlank(badData) ? badData : "OK");
        }
    }

    public List<Game> fetchGames(int seasonYear, LocalDate localDate, User user) {
        return fetchGames(localDate, seasonRepository.findByYear(seasonYear).getFirst(), user);
    }

    public record ScheduleStatus(TeamStatus teamStatus, ConferenceStatus conferenceStatus, List<SeasonStatus> seasons) {

    }

    public record TeamStatus(long numberOfTeams, String teamStatus) {
    }

    public record ConferenceStatus(long numberOfConferences, String conferenceStatus) {
    }

    public record SeasonStatus(int year, long numberOfTeams, long numberOfConferences, long numberOfGames,
                               LocalDate firstGameDate, LocalDate lastGameDate, LocalDate lastCompleteGameDate,
                               Timestamp lastUpdated) {
    }

    @Transactional
    public int dropSeason(int seasonYear, User user) {
        LocalDateTime start = LocalDateTime.now();
        Season s = seasonRepository.findByYear(seasonYear).getFirst();
        deleteMappings(s, user);
        long gamesDeleted = gameRepository.deleteBySeason(s);
        teamStatisticRepository.deleteBySeason(s);
        seasonRepository.delete(s);
        LocalDateTime end = LocalDateTime.now();
        auditRepository.save(new Audit(0L, "dropSeason", "%d mappings deleted".formatted(gamesDeleted), Timestamp.valueOf(start), Timestamp.valueOf(end), user));

        return (int) gamesDeleted;
    }

}
