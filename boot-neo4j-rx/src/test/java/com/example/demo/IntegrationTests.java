package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class IntegrationTests {

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

  @Autowired WebTestClient client;

  @Test
  void contextLoads() {
    this.client
        .get()
        .uri("/posts")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(Post.class)
        .hasSize(2);
  }
}
