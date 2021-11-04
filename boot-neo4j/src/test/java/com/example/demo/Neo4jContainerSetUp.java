package com.example.demo;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;

abstract class Neo4jContainerSetUp {

  @Container
  static Neo4jContainer<?> neo4JContainer =
      new Neo4jContainer<>("neo4j:4.0").withStartupTimeout(Duration.ofMinutes(5));

  static {
    neo4JContainer.start();
  }

  @DynamicPropertySource
  static void neo4jProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.neo4j.uri", neo4JContainer::getBoltUrl);
    registry.add("spring.neo4j.authentication.username", () -> "neo4j");
    registry.add("spring.neo4j.authentication.password", neo4JContainer::getAdminPassword);
  }
}
