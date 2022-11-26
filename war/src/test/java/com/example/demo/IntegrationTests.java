package com.example.demo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author hantsy
 */
public class IntegrationTests {

  WebTestClient rest;

  @BeforeEach
  public void setup() {
    this.rest = WebTestClient
        .bindToServer()
        //.defaultHeaders(headers -> headers.setBasicAuth("user", "password"))
        .responseTimeout(Duration.ofMillis(1000))
        .baseUrl("http://localhost:9000")
        .build();
  }

  @Test
  public void getAllPosts() throws Exception {
    this.rest
        .get()
        .uri("/posts")
        .exchange()
        .expectStatus().isOk();
  }
}
