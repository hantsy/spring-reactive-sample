package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.concurrent.TimeUnit;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {DemoApplication.class})
@AutoConfigureWebTestClient
class IntegrationTests extends Neo4jContainerSetUp {

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
