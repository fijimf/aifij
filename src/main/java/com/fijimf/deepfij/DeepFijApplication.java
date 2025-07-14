package com.fijimf.deepfij;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.fijimf.deepfij.model.User;
import com.fijimf.deepfij.repo.UserRepository;
import com.fijimf.deepfij.service.UserService;

import jakarta.validation.constraints.NotNull;

@SpringBootApplication
public class DeepFijApplication {
    public static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DeepFijApplication.class);
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DeepFijApplication.class, args);
        UserService userMgr = context.getBean(UserService.class);
        UserRepository userRepository = context.getBean(UserRepository.class);
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