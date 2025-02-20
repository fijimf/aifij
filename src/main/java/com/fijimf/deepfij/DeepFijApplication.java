package com.fijimf.deepfij;

import com.fijimf.deepfij.model.User;
import com.fijimf.deepfij.repo.UserRepository;
import com.fijimf.deepfij.service.UserService;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class DeepFijApplication {
    public static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DeepFijApplication.class);
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DeepFijApplication.class, args);
        UserService userMgr = context.getBean(UserService.class);
        UserRepository userRepository = context.getBean(UserRepository.class);
        PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
        String password = getTempAdminPassword();
        User u = userRepository.findByUsername("admin");
        if (u==null) {
            userMgr.createUser("admin", password,List.of("USER", "ADMIN"));
        } else {
            userMgr.updatePassword(u, password);
        }
        logger.info("admin password is {}", password);

    }

    @NotNull
    private static String getTempAdminPassword() {
        String p = System.getProperty("admin.password");
        if (StringUtils.isNotBlank(p)) {
            return p;
        } else {
            RandomStringGenerator rsg = new RandomStringGenerator.Builder().withinRange(new char[]{'a', 'z'}).build();
            return rsg.generate(6);
        }
    }
}