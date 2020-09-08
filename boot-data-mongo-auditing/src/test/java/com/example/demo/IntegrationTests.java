package com.example.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTests {

    @LocalServerPort
    int port;

    WebTestClient client;


    @BeforeEach
    public void setup() {

        this.client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @Test
    public void getAllMessagesShouldBeOk() {
        client.get().uri("/posts")
                .exchange()
                .expectStatus().isOk();
    }


    @Test
    public void testInsert() {
        var data = Post.builder().title("test").content("content").build();
        client.post().uri("/posts").bodyValue(data)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("test")
                .jsonPath("$.createdAt").exists()
                .jsonPath("$.updatedAt").exists()
        ;
    }

}
