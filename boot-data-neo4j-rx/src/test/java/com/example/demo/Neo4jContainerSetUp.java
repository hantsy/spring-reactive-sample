package com.example.demo;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;

abstract class Neo4jContainerSetUp {

    @Container
    static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:latest")
            .withStartupTimeout(Duration.ofMinutes(5));

    static {
        neo4jContainer.start();
    }

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("org.neo4j.driver.uri", neo4jContainer::getBoltUrl);
        registry.add("org.neo4j.driver.authentication.username", () -> "neo4j");
        registry.add("org.neo4j.driver.authentication.password", neo4jContainer::getAdminPassword);
    }
}
