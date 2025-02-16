package com.fijimf.deepfij.repo;

import org.testcontainers.containers.PostgreSQLContainer;

public class SharedPostgreSQLContainer extends PostgreSQLContainer<SharedPostgreSQLContainer> {
    private static final String IMAGE_VERSION = "postgres:15";
    private static SharedPostgreSQLContainer container;

    private SharedPostgreSQLContainer() {
        super(IMAGE_VERSION);
        this.withDatabaseName("deepfij")
                .withUsername("postgres")
                .withPassword("p@ssw0rd");
    }

    public static SharedPostgreSQLContainer getInstance() {
        if (container == null) {
            container = new SharedPostgreSQLContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        // Do nothing, JVM will handle container shutdown
    }
}

