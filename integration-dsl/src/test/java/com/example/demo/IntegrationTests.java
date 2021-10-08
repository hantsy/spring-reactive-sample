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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

/**
 *
 * @author hantsy
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Application.class)
public class IntegrationTests {

   @Value("${server.port:8080}")
    int port;

    WebTestClient rest;

    @Autowired HttpServer httpServer;

    private DisposableServer disposableServer;

    @BeforeAll
    public void setup() {
        this.disposableServer = this.httpServer.bindNow();
        this.rest =
            WebTestClient.bindToServer()
                .responseTimeout(Duration.ofDays(1))
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @AfterAll
    void tearDown() {
        disposableServer.disposeNow();
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
