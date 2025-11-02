package com.fijimf.deepfij;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeepFijApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeepFijApplication.class, args);
    }
}