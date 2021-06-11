package com.example.demo;

import com.example.demo.config.CassandraContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureWebTestClient
class IntegrationTests extends CassandraContainerConfig {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void contextLoads() {
        this.webTestClient.get().uri("/posts")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Post.class).hasSize(2);
    }

}
