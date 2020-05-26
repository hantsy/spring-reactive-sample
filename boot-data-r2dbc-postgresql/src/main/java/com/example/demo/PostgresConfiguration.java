package com.example.demo;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
@Configuration
public class PostgresConfiguration extends AbstractR2dbcConfiguration {

    public static final String POSTGRES_BEAN = "postgresLocal";
    public static final String CONNECTION_FACTORY = "postgresConnectionFactory";

    @Bean(name = POSTGRES_BEAN, destroyMethod = "close")
    public PostgreSQLContainer producePostgreSQLContainer() {
        log.info("Starting PostgreSQL");
        var postgreSQLContainer = new PostgreSQLContainer("postgres:11.8");

        postgreSQLContainer.start();

        log.info(String.format("Port number: %s", postgreSQLContainer.getFirstMappedPort()));
        log.info(String.format("Host: %s", postgreSQLContainer.getContainerIpAddress()));

        return postgreSQLContainer;
    }

    @Bean(name = CONNECTION_FACTORY)
    @DependsOn(POSTGRES_BEAN)
    @Override
    public ConnectionFactory connectionFactory() {
        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host("localhost")
                        .port(producePostgreSQLContainer().getFirstMappedPort())
                        .username("test")
                        .password("test")
                        .database("test")
                        .build());
    }
}
