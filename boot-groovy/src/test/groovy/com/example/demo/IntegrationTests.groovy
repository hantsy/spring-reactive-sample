package com.example.demo


import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient

import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTests {

    @LocalServerPort
    int port;

    WebTestClient client

    @BeforeEach
    void setup() {
        this.client = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofSeconds(10))
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @Test
    void getAllPostsWillBeOk() throws Exception {
        this.client
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus()
                .isOk();
    }

}
