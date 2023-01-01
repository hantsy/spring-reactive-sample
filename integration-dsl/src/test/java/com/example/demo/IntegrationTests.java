package com.example.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    private WebTestClient rest;

    @Autowired
    HttpServer httpServer;

    private DisposableServer disposableServer;

    @BeforeEach
    public void setup() {
        this.disposableServer = this.httpServer.bindNow(Duration.ofMillis(5000));
        this.rest = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofMillis(5000))
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @AfterEach
    public void tearDown() {
        this.disposableServer.dispose();
    }

    @Test
    public void getAllPostsWillBeOk() throws Exception {
        this.rest
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void getAllPostsWillBeOk_viaIntegration() throws Exception {
        this.rest
                .get()
                .uri("/all")
                .exchange()
                .expectStatus().isOk();
    }

}
