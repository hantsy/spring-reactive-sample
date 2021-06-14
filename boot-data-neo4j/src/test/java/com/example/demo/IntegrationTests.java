package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = DemoApplication.class)
@ContextConfiguration(initializers = PostRepositoryTest.TestContainerInitializer.class)
@AutoConfigureWebTestClient
class IntegrationTests {

    @Autowired
    WebTestClient client;

    @Test
    void contextLoads() {
        this.client.get().uri("/posts")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Post.class).hasSize(2);
    }

}
