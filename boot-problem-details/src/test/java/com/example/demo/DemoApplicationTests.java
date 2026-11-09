package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.UUID;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
public class DemoApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:latest"));

    @Autowired
    WebTestClient client;

    @Test
    public void getAllPostsWillBeOk() {
        this.client
                .get().uri("/posts")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1);
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
                .jsonPath("$.type").isEqualTo("https://example.com/errors/post-not-found")
                .jsonPath("$.id").isEqualTo(postId.toString())
                .jsonPath("$.entity").isEqualTo("POST");
    }

    @Test
    public void createPost_whenBodyInvalid() {
        this.client
                .post().uri("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("title", "test"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.errors[0].path").isEqualTo("content");
    }

    @Test
    public void getPost_whenIdIsNotUuid() {
        this.client
                .get().uri("/posts/{id}", "not-a-uuid")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
    }
}
