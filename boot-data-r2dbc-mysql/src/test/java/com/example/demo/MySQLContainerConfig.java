package com.example.demo;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public abstract class MySQLContainerConfig {

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("mysql:8");

    @Container
    static MySQLContainer mySQLContainer = new MySQLContainer(DEFAULT_IMAGE_NAME);

    static {
        mySQLContainer.start();
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:mysql://"
                + mySQLContainer.getHost() + ":" + mySQLContainer.getFirstMappedPort()
                + "/" + mySQLContainer.getDatabaseName());
        registry.add("spring.r2dbc.username", () -> mySQLContainer.getUsername());
        registry.add("spring.r2dbc.password", () -> mySQLContainer.getPassword());
    }
}
