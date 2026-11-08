package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestDemoApplication2 {

    @Bean
    PostgreSQLContainer postgreSQLContainer(DynamicPropertyRegistry registry) {
        PostgreSQLContainer PG_CONTAINER = new PostgreSQLContainer(DockerImageName.parse("postgres:14"));

        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + PG_CONTAINER.getHost() + ":"
                + PG_CONTAINER.getFirstMappedPort() + "/" + PG_CONTAINER.getDatabaseName());
        registry.add("spring.r2dbc.username", PG_CONTAINER::getUsername);
        registry.add("spring.r2dbc.password", PG_CONTAINER::getPassword);

        return PG_CONTAINER;
    }

    public static void main(String[] args) {
        SpringApplication.from(DemoApplication::main)
                .with(TestDemoApplication2.class)
                .run(args);
    }
}
