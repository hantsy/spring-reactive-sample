/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author hantsy
 */
@SpringJUnitConfig(classes = Application.class)
@ActiveProfiles("test")
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
  public void getAllPostsWithoutUserQueryParams_shouldReturn401() throws Exception {
    this.rest
        .get()
        .uri("/posts")
        .exchange()
        .expectStatus().isUnauthorized();
  }

  @Test
  public void getAllPostsWithUserQueryParams_shouldBeOK() throws Exception {
    this.rest
        .get()
        .uri("/posts?user=user")
        .exchange()
        .expectStatus().isOk();
  }

}
