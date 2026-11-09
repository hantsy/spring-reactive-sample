package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

@SpringBootTest(properties = "demo.client-runner.enabled=false")
@AutoConfigureWebTestClient
public class PostApiControllerTest {

    @Autowired
    WebTestClient client;

    @Test
    public void testGetAllPosts() {
        client.get().uri("/posts")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void testGetPost_NotFound() {
        var id = UUID.randomUUID();
        client.get().uri("/posts/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString())
                .jsonPath("$.detail").isEqualTo("Post: " + id + " not found");
    }
}