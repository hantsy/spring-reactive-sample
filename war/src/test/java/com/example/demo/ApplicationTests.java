package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author hantsy
 */
@SpringJUnitWebConfig(classes = {AppConfig.class, WebConfig.class})
public class ApplicationTests {

  @Autowired
  ApplicationContext context;

  WebTestClient rest;

  @BeforeEach
  public void setup() {
    this.rest = WebTestClient
        .bindToApplicationContext(this.context)
        .configureClient()
        .build();
  }

  @Test
  public void getAllPostsWillBeOk() throws Exception {
    this.rest
        .get()
        .uri("/posts")
        .exchange()
        .expectStatus().isOk();
  }
}
