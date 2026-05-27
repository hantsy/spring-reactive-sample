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
import com.redis.testcontainers.RedisContainer;

import static com.redis.testcontainers.RedisContainer.DEFAULT_IMAGE_NAME;
import static com.redis.testcontainers.RedisContainer.DEFAULT_TAG;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:18.4-alpine"))
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("init.sql"),
                    "/docker-entrypoint-initdb.d/init.sql");

    private static final RedisContainer REDIS = new RedisContainer(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));

    @Bean
    PostgreSQLContainer postgresContainer() {
        return POSTGRES;
    }

    @Bean
    public RedisContainer redisContainer() {
        return REDIS;
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        if (!REDIS.isRunning()) {
            REDIS.start();
        }

        context.addApplicationListener((event) -> {
            if (event instanceof ContextClosedEvent) {
                POSTGRES.stop();
                REDIS.stop();
            }
        });
        TestPropertyValues.of(
                "r2dbc.host=" + POSTGRES.getHost(),
                "r2dbc.port=" + POSTGRES.getFirstMappedPort(),
                "r2dbc.username=" + POSTGRES.getUsername(),
                "r2dbc.password=" + POSTGRES.getPassword(),
                "r2dbc.database=" + POSTGRES.getDatabaseName(),
                "redis.host=" + REDIS.getHost(),
                "redis.port=" + REDIS.getFirstMappedPort()
        ).applyTo(context.getEnvironment());
    }

}
