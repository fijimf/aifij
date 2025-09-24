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
import com.fijimf.deepfij.repo.UserRepository;

import jakarta.validation.constraints.NotNull;

@Service
public class StartupService {
    
    private static final Logger logger = LoggerFactory.getLogger(StartupService.class);
    
    private final UserService userService;
    private final UserRepository userRepository;
    private final InitializationService initializationService;
    
    public StartupService(UserService userService, UserRepository userRepository, 
                          InitializationService initializationService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.initializationService = initializationService;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application startup complete - initializing admin user and database");
        initializeAdminUser();
        initializationService.performInitialization();
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