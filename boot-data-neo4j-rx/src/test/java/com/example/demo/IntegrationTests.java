package com.example.demo;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.springframework.boot.test.autoconfigure.Neo4jTestHarnessAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.Neo4jContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = Neo4jTestHarnessAutoConfiguration.class)
@AutoConfigureWebTestClient
class IntegrationTests extends Neo4jContainerSetUp {

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
