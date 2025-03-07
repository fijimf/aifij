package com.fijimf.deepfij.service;

import com.fijimf.deepfij.model.schedule.*;
import com.fijimf.deepfij.model.scraping.scoreboard.ScoreboardResponse;
import com.fijimf.deepfij.model.scraping.standings.ConferenceStanding;
import com.fijimf.deepfij.model.scraping.standings.StandingsEntry;
import com.fijimf.deepfij.model.scraping.standings.StandingsResponse;
import com.fijimf.deepfij.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ScheduleService {
    private final ScrapingService scrapingService;
    private final TeamRepository teamRepository;
    private final ConferenceRepository conferenceRepository;
    private final ConferenceMappingRepository conferenceMappingRepository;
    private final GameRepository gameRepository;
    private final SeasonRepository seasonRepository;
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    public ScheduleService(@Autowired ScrapingService scrapingService, @Autowired TeamRepository teamRepository, @Autowired ConferenceRepository conferenceRepository, @Autowired ConferenceMappingRepository conferenceMappingRepository, @Autowired GameRepository gameRepository, @Autowired SeasonRepository seasonRepository) {
        this.scrapingService = scrapingService;
        this.teamRepository = teamRepository;
        this.conferenceRepository = conferenceRepository;
        this.conferenceMappingRepository = conferenceMappingRepository;
        this.gameRepository = gameRepository;
        this.seasonRepository = seasonRepository;
    }

    public List<Conference> loadConferences() {
        scrapingService.fetchConferences().forEach(c -> conferenceRepository.save(c.toConference()));
        return conferenceRepository.findAll();
    }

    public List<Team> loadTeams() {
        scrapingService.fetchTeams()
                .getFirst()
                .leagues()
                .getFirst()
                .teams()
                .forEach(w -> {
                    teamRepository.save(w.toTeam());
                });
        return teamRepository.findAll();
    }

    public List<Game> fetchGames(LocalDate d) {
        ScoreboardResponse scoreboard = scrapingService.fetchScoreboard(Integer.parseInt(d.format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
        return scoreboard.sports().stream().flatMap(sport -> {
            return sport.leagues().stream().flatMap(league -> {
                return league.events().stream().flatMap(event -> {
                    Game g = new Game();
                    g.setDate(event.date().toLocalDate());
                    g.setEspnId(event.id());
                    g.setLocation(event.location());
                    g.setNeutralSite(event.neutralSite());

                    g.setPeriods(event.fullStatus().period());
                    event.competitors().forEach(competitor -> {
                        if (competitor.homeAway().equals("home")) {
                            teamRepository.findByEspnId(competitor.id()).stream().findFirst().ifPresentOrElse(t -> {
                                g.setHomeTeam(t);
                                g.setHomeScore(Integer.parseInt(competitor.score()));
                                g.setHomeTeamSeed(competitor.tournamentMatchup().seed());
                            }, () -> {
                                logger.error("Team " + competitor.name() + " not found in database");
                            });
                        } else if (competitor.homeAway().equals("away")) {
                            teamRepository.findByEspnId(competitor.id()).stream().findFirst().ifPresentOrElse(t -> {
                                g.setAwayTeam(t);
                                g.setAwayScore(Integer.parseInt(competitor.score()));
                                g.setAwayTeamSeed(competitor.tournamentMatchup().seed());
                            }, () -> {
                                logger.error("Team " + competitor.name() + " not found in database");
                            });
                        } else {
                            // Should not be here
                        }
                    });
                    g.setOverUnder(event.odds().overUnder());
                    g.setSpread(event.odds().spread());
                    g.setAwayMoneyLine(event.odds().away().moneyLine());
                    g.setHomeMoneyLine(event.odds().home().moneyLine());
                    if (g.getHomeTeam() != null && g.getAwayTeam() != null) {
                        return Stream.of(g);
                    } else {
                        return Stream.empty();
                    }
                });


            });
        }).toList();

    }


    public Season findOrCreate(int yyyy) {
        List<Season> seasonList = seasonRepository.findByYear(yyyy);
        if (!seasonList.isEmpty()) return seasonList.getFirst();
        else {
            Season season = new Season();
            season.setId(0L);
            season.setName((yyyy - 1) + "-" + yyyy % 100);
            season.setStartDate(LocalDate.of(yyyy - 1, 11, 1));
            season.setEndDate(LocalDate.of(yyyy, 4, 30));
            season.setYear(yyyy);
            return seasonRepository.save(season);
        }
    }

    public void createSchedule(int yyyy) {
        if (teamRepository.count() == 0) loadTeams();
        if (conferenceRepository.count() == 0) loadConferences();

        Season s = findOrCreate(yyyy);

        StandingsResponse standingsResponse = scrapingService.fetchStandings(yyyy);

        standingsResponse.children().forEach(cs -> {
            Conference c = findOrCreateConference(cs);
            cs.consolidatedStandings().forEach(se -> {
                Team t = findOrCreateTeam(se);
                ConferenceMapping mapping = new ConferenceMapping();
                mapping.setSeason(s);
                mapping.setConference(c);
                mapping.setTeam(t);
                conferenceMappingRepository.save(mapping);
            });
        });

        logger.info("For " + yyyy + " there are " + conferenceMappingRepository.count() + " teams");

        Stream.iterate(s.getStartDate(), date -> !date.isAfter(s.getEndDate()), date -> date.plusDays(1)).forEach(
                d->{
                   gameRepository.saveAll(fetchGames(d));
                }
        );
    }

    private Team findOrCreateTeam(StandingsEntry se) {
        List<Team> teams = teamRepository.findByEspnId(se.rawTeam().id());
        if (!teams.isEmpty()) {
            return teams.getFirst();
        } else {
            return teamRepository.save(se.rawTeam().getTeam());
        }
    }

    public Conference findOrCreateConference(ConferenceStanding cs) {
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


}
