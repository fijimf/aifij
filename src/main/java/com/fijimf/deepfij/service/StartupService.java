package com.fijimf.deepfij.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.fijimf.deepfij.model.User;
import com.fijimf.deepfij.repo.ConferenceRepository;
import com.fijimf.deepfij.repo.GameRepository;
import com.fijimf.deepfij.repo.TeamRepository;
import com.fijimf.deepfij.repo.UserRepository;

import jakarta.validation.constraints.NotNull;

@Service
public class StartupService {
    
    private static final Logger logger = LoggerFactory.getLogger(StartupService.class);
    
    private final UserService userService;
    private final UserRepository userRepository;
    private final ScheduleService scheduleService;
    private final TeamRepository teamRepository;
    private final ConferenceRepository conferenceRepository;
    private final GameRepository gameRepository;
    private final String seasonYears;
    
    public StartupService(UserService userService, UserRepository userRepository, ScheduleService scheduleService,
                          TeamRepository teamRepository, ConferenceRepository conferenceRepository,
                          GameRepository gameRepository, @Value("${deepfij.seasons_to_load}") String seasonYears) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.scheduleService = scheduleService;
        this.teamRepository = teamRepository;
        this.conferenceRepository = conferenceRepository;
        this.gameRepository = gameRepository;
        this.seasonYears = seasonYears;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application startup complete - initializing admin user and checking schedule");
        User adminUser = initializeAdminUser();
        checkAndInitializeSchedule();
    }
    
    private User initializeAdminUser() {
        String password = getTempAdminPassword();
        User u = userRepository.findByUsername("admin");
        if (u == null) {
            userService.createUser("admin", password, List.of("USER", "ADMIN"));
            u = userRepository.findByUsername("admin");
            logger.info("Created admin user with temporary password");
        } else {
            userService.updatePassword(u, password);
            logger.info("Updated admin user password");
        }
        logger.info("admin password is {}", password);
        return u;
    }
    
    private void checkAndInitializeSchedule() {
        long teamCount = teamRepository.count();
        long conferenceCount = conferenceRepository.count();
        long gameCount = gameRepository.count();
        
        logger.info("Current data counts - Teams: {}, Conferences: {}, Games: {}", teamCount, conferenceCount, gameCount);
        
        if (teamCount == 0 && conferenceCount == 0 && gameCount == 0) {
            logger.info("No schedule data found - initializing teams from ESPN");
            scheduleService.loadTeams();
            scheduleService.loadConferences();
            logger.info("Teams and conferences initialized");

            String[] years = seasonYears.split(",");
            for (String year : years) {
                scheduleService.createSchedule(Integer.parseInt(year.trim()));
            }
            logger.info("Schedule initialization completed");
        } else {
            logger.info("Schedule data already exists - skipping initialization");
        }
    }
    
    @NotNull
    private String getTempAdminPassword() {
        String p = System.getProperty("admin.password");
        if (StringUtils.isNotBlank(p)) {
            return p;
        } else {
            // Generate a password that meets complexity requirements
            RandomStringGenerator lowercase = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
            RandomStringGenerator uppercase = new RandomStringGenerator.Builder().withinRange('A', 'Z').build();
            RandomStringGenerator digits = new RandomStringGenerator.Builder().withinRange('0', '9').build();
            
            // Build password with required character types (8 characters total)
            return uppercase.generate(2) +    // 2 uppercase letters
                   lowercase.generate(3) +    // 3 lowercase letters  
                   digits.generate(2) +       // 2 digits
                   "!";                       // 1 special character
        }
    }
}