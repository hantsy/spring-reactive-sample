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
import java.util.concurrent.TimeUnit;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {DemoApplication.class})
@AutoConfigureWebTestClient
@Testcontainers
class IntegrationTests {

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

  @Autowired WebTestClient webTestClient;

  @Test
  void contextLoads() throws InterruptedException {
    // waiting so that posts are inserted
    TimeUnit.SECONDS.sleep(3);
    this.webTestClient
        .get()
        .uri("/posts")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(Post.class)
        .hasSize(2);
  }
}
