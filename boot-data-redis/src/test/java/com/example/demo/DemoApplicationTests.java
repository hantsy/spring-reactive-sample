package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.net.URI;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

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
        .baseUrl("http://localhost:8080/")
        .build();
  }

  @Test
  public void getAllPosts() {
    client
        .get()
        .uri("/posts/")
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  public void getANoneExistedPostShouldReturn404() {
    client
        .get()
        .uri("/posts/"+UUID.randomUUID().toString())
        .exchange()
        .expectStatus().isNotFound();
  }

}