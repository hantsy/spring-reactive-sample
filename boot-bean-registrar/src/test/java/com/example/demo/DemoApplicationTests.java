package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class DemoApplicationTests {

    @Autowired
    PostRepository postRepository;

    @Autowired
    WebTestClient client;

    @Test
    void usesInMemoryRepositoryByDefault() {
        assertThat(this.postRepository).isInstanceOf(InMemoryPostRepository.class);
    }

    @Test
    void getAllPosts() {
        this.client.get().uri("/posts").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Post.class)
                .value(posts -> assertThat(posts).hasSizeGreaterThanOrEqualTo(2));
    }

    @Test
    void getMissingPostReturns404() {
        this.client.get().uri("/posts/" + UUID.randomUUID()).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void crudOperations() {
        var location = this.client.post().uri("/posts").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new Post(null, "Test Title", "Test Content"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .returnResult(Void.class)
                .getResponseHeaders().getLocation();
        assertThat(location).isNotNull();
        var path = location.getPath();

        this.client.get().uri(path).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Test Title")
                .jsonPath("$.content").isEqualTo("Test Content");

        this.client.put().uri(path).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new Post(null, "Updated Title", "Updated Content"))
                .exchange()
                .expectStatus().isNoContent();

        this.client.get().uri(path).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.title").isEqualTo("Updated Title");

        this.client.delete().uri(path)
                .exchange()
                .expectStatus().isNoContent();

        this.client.get().uri(path).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}