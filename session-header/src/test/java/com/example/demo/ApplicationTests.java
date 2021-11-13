/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author hantsy
 */
@SpringJUnitConfig(classes = Application.class)
@TestPropertySource(properties = "server.session.timeout:1")
public class ApplicationTests {

    @Value("${server.port:8080}")
    int port;

    WebTestClient rest;

    @Autowired
    HttpServer httpServer;

    DisposableServer disposableServer;

    @BeforeEach
    public void setup() {
        this.disposableServer = this.httpServer.bindNow();
        this.rest = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofMinutes(1))
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @AfterEach
    public void teardown() {
        if (!this.disposableServer.isDisposed()) {
            this.disposableServer.disposeNow();
        }
    }

    @Test
    public void getAllPostsWillBeOk() throws Exception {
        this.rest
                .get()
                .uri("/posts")
                .header("Authorization", getBasicAuth())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void userDefinedMappingsSecureByDefault() throws Exception {

        FluxExchangeResult<Map<String, String>> result = this.rest
                .get()
                .uri("/sessionId")
                .header("Authorization", getBasicAuth())
                .exchange()
                .returnResult(new ParameterizedTypeReference<Map<String, String>>() {
                });

        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK);

        List<String> sessionHeaders = result.getResponseHeaders().get("X-SESSION-ID");
        assertThat(sessionHeaders.size()).isEqualTo(1);

        String sessionId = result.getResponseBody().blockFirst().get("id");

        assertThat(sessionHeaders.get(0)).isEqualTo(sessionId);

        this.rest
                .get()
                .uri("/posts")
                .header("X-SESSION-ID", sessionId)
                .exchange()
                .expectStatus().isOk();
    }

    private String getBasicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString("user:password".getBytes());
    }

}
