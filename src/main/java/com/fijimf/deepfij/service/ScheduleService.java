package com.fijimf.deepfij.service;

import com.fijimf.deepfij.model.schedule.Conference;
import com.fijimf.deepfij.model.schedule.Team;
import com.fijimf.deepfij.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {
    private final ScrapingService scrapingService;
    private final TeamRepository teamRepository;
    private final ConferenceRepository conferenceRepository;
    private final ConferenceMappingRepository conferenceMappingRepository;
    private final GameRepository gameRepository;
    private final SeasonRepository seasonRepository;

    public ScheduleService(@Autowired ScrapingService scrapingService, @Autowired TeamRepository teamRepository, @Autowired ConferenceRepository conferenceRepository, @Autowired ConferenceMappingRepository conferenceMappingRepository, @Autowired GameRepository gameRepository, @Autowired SeasonRepository seasonRepository) {
        this.scrapingService = scrapingService;
        this.teamRepository = teamRepository;
        this.conferenceRepository = conferenceRepository;
        this.conferenceMappingRepository = conferenceMappingRepository;
        this.gameRepository = gameRepository;
        this.seasonRepository = seasonRepository;
    }

    public List<Conference> loadConferences() {
        scrapingService.fetchConferences().forEach(c-> conferenceRepository.save(c.toConference()));
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





}
