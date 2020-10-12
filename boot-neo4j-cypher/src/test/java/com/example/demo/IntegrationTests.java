package com.example.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

class IntegrationTests {

    WebTestClient client;

    @BeforeAll
    public void setup() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Test
    void contextLoads() {
        this.client.get().uri("/posts")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Post.class).hasSize(2);
    }

}
