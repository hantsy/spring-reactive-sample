package com.example.demo;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
public class DemoApplicationTests {

  @Autowired
  ApplicationContext context;

  WebTestClient client;

  @BeforeEach
  public void setup() {
    client = WebTestClient
        .bindToApplicationContext(context)
        .configureClient()
        .build();
  }

  @Test
  public void getAllPosts() {
    client
        .get()
        .uri("/posts")
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  public void getANoneExistedPostShouldReturn404() {
    client
        .get()
        .uri("/posts/" + UUID.randomUUID().toString())
        .exchange()
        .expectStatus().isNotFound();
  }

}