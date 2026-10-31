package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;


@Slf4j
class PostgresContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:latest");
//                    .withCopyFileToContainer(MountableFile.forClasspathResource("init.sql"), "/docker-entrypoint-initdb.d/init.sql");
        postgreSQLContainer.start();
        log.info(" container.getFirstMappedPort():: {}", postgreSQLContainer.getFirstMappedPort());

        ctx.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                postgreSQLContainer.stop();
            }
        });

        ctx.getEnvironment().getPropertySources()
                .addLast(
                        new MapPropertySource(
                                "r2dbc",
                                Map.of("r2dbc.host", postgreSQLContainer.getHost(),
                                        "r2dbc.port", postgreSQLContainer.getFirstMappedPort(),
                                        "r2dbc.databaseName", postgreSQLContainer.getDatabaseName(),
                                        "r2dbc.username", postgreSQLContainer.getUsername(),
                                        "r2dbc.password",  postgreSQLContainer.getPassword()
                                )
                        )
                );
    }
}