/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** @author hantsy */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource(properties = "server.session.timeout:1")
class ApplicationTests {

  @Value("${server.port:8080}")
  int port;

  @Autowired HttpServer httpServer;
  private DisposableServer disposableServer;

  WebTestClient rest;

  @BeforeAll
  public void setup() {
    this.disposableServer = httpServer.bindNow();
    this.rest =
        WebTestClient.bindToServer()
            .responseTimeout(Duration.ofMinutes(1))
            .baseUrl("http://localhost:" + this.port)
            .build();
  }

  @AfterAll
  void tearDown() {
    this.disposableServer.dispose();
  }

  @Test
  void getAllPostsWillBeOk() throws Exception {
    this.rest
        .get()
        .uri("/posts")
        .header("Authorization", getBasicAuth())
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void userDefinedMappingsSecureByDefault() {

    FluxExchangeResult<Map<String, String>> result =
        this.rest
            .get()
            .uri("/sessionId")
            .header("Authorization", getBasicAuth())
            .exchange()
            .returnResult(new ParameterizedTypeReference<Map<String, String>>() {});

    assertThat(result.getStatus()).isEqualTo(HttpStatus.OK);

    List<String> sessionHeaders = result.getResponseHeaders().get("X-SESSION-ID");
    assertThat(sessionHeaders).isNotEmpty();
    assertThat(sessionHeaders.size()).isEqualTo(1);

    String sessionId = result.getResponseBody().blockFirst().get("id");

    assertThat(sessionHeaders.get(0)).isEqualTo(sessionId);

    this.rest
        .get()
        .uri("/posts")
        .header("X-SESSION-ID", sessionId)
        .exchange()
        .expectStatus()
        .isOk();
  }

  private String getBasicAuth() {
    return "Basic " + Base64.getEncoder().encodeToString("user:password".getBytes());
  }
}
