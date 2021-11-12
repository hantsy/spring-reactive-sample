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
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;

/**
 * @author hantsy
 */
@SpringJUnitConfig(classes = Application.class)
public class IntegrationTests {

    @Autowired
    HttpServer httpServer;

    private DisposableServer disposableServer;

    int port = 8080;

    WebTestClient rest;

    @BeforeEach
    public void setup() {
        this.disposableServer = httpServer.bindNow();
        this.rest = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofMillis(1000))
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
                .exchange()
                .expectStatus().isOk();
    }

}
