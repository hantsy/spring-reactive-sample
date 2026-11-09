package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;

@Slf4j
public class PostgresContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var container = new PostgreSQLContainer("postgres:latest");
        container.start();
        log.info("PostgreSQL container started at {}:{}", container.getHost(), container.getFirstMappedPort());

        context.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                container.stop();
            }
        });

        context.getEnvironment().getPropertySources()
                .addFirst(
                        new MapPropertySource("r2dbc",
                                Map.of(
                                        "r2dbc.host", container.getHost(),
                                        "r2dbc.port", container.getFirstMappedPort(),
                                        "r2dbc.database", container.getDatabaseName(),
                                        "r2dbc.username", container.getUsername(),
                                        "r2dbc.password", container.getPassword()
                                )
                        )
                );
    }
}
