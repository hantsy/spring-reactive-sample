package com.example.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${server.port:8080}")
    int port;

    WebTestClient rest;

    @Autowired
    HttpServer httpServer;

    private DisposableServer disposableServer;

    @BeforeAll
    public void setup() {
        this.disposableServer = this.httpServer.bindNow();
        this.rest = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofDays(1))
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @AfterAll
    public void teardown() {
        this.disposableServer.dispose();
    }

    @Test
    public void getAllPostsWillBeOk() throws Exception {
        this.rest
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus()
                .isOk();
    }

}
