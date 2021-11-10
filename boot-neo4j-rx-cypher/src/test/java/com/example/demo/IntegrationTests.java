package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class IntegrationTests extends Neo4jContainerSetUp {

  @Autowired WebTestClient webTestClient;

  @Test
  void contextLoads() {
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
