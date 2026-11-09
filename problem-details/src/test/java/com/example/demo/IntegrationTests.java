package com.example.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.util.UUID;

@SpringJUnitConfig
@ContextConfiguration(classes = Application.class, initializers = PostgresContextInitializer.class)
@TestPropertySource(properties = "server.port=0")
public class IntegrationTests {

    @Autowired
    HttpServer httpServer;

    private DisposableServer disposableServer;

    private WebTestClient client;

    @BeforeEach
    public void setup() {
        this.disposableServer = this.httpServer.bindNow();
        this.client = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + this.disposableServer.port())
                .build();
    }

    @AfterEach
    public void teardown() {
        this.disposableServer.dispose();
    }

    @Test
    public void getAllPostsWillBeOk() {
        this.client
                .get().uri("/posts")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void getPost_whenNotFound() {
        var postId = UUID.randomUUID();

        this.client
                .get().uri("/posts/{id}", postId)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(postId.toString())
                .jsonPath("$.entity").isEqualTo("POST");
    }
}
