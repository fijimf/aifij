package com.fijimf.deepfij.repo;

import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.util.Properties;

public class SharedPostgreSQLContainer extends PostgreSQLContainer<SharedPostgreSQLContainer> {
    
    private static final String IMAGE_VERSION = "postgres:15";
    private static SharedPostgreSQLContainer container;
    private static final Properties testProperties = loadTestProperties();

    private SharedPostgreSQLContainer() {
        super(testProperties.getProperty("testcontainers.postgres.image", IMAGE_VERSION));
        this.withDatabaseName(testProperties.getProperty("testcontainers.postgres.database", "deepfij"))
            .withUsername(testProperties.getProperty("testcontainers.postgres.username", "postgres"))
            .withPassword(testProperties.getProperty("testcontainers.postgres.password", "p@ssw0rd"))
            .withReuse(true);
    }

    private static Properties loadTestProperties() {
        Properties props = new Properties();
        try {
            props.load(SharedPostgreSQLContainer.class.getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            // Fallback to default values if properties file can't be loaded
            props.setProperty("testcontainers.postgres.image", IMAGE_VERSION);
            props.setProperty("testcontainers.postgres.database", "deepfij");
            props.setProperty("testcontainers.postgres.username", "postgres");
            props.setProperty("testcontainers.postgres.password", "p@ssw0rd");
        }
        return props;
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
    }

    @Override
    public void stop() {
        // Do nothing, JVM handles shut down
    }
}