package com.example.demo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.test.util.TestPropertyValues;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:18.4-alpine"))
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("init.sql"),
                    "/docker-entrypoint-initdb.d/init.sql");

    @Bean
    PostgreSQLContainer postgresContainer() {
        return POSTGRES;
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
         if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        context.addApplicationListener((ContextClosedEvent event) -> POSTGRES.stop());
        TestPropertyValues.of(
                "r2dbc.host=" + POSTGRES.getHost(),
                "r2dbc.port=" + POSTGRES.getFirstMappedPort(),
                "r2dbc.username=" + POSTGRES.getUsername(),
                "r2dbc.password=" + POSTGRES.getPassword(),
                "r2dbc.database=" + POSTGRES.getDatabaseName()
        ).applyTo(context.getEnvironment());
    }
}
