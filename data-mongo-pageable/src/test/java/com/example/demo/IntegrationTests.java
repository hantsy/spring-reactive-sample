package com.example.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;

/**
 * @author hantsy
 */
@SpringJUnitConfig(classes = Application.class)
public class IntegrationTests {

    @Value("${server.port:8080}")
    int port;

    WebTestClient client;

    @Autowired
    HttpServer httpServer;

    private DisposableServer disposableServer;

    @BeforeAll
    public void setup() {
        this.disposableServer = this.httpServer.bindNow();
        this.client = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofDays(1))
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @AfterAll
    public void teardown() {
        this.disposableServer.disposeNow();
    }

    @Test
    public void searchPostsByKeyword_shouldBeOK() throws Exception {
        this.client
                .get()
                .uri("/posts/search?q=post")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Post.class).hasSize(10);
    }

    @Test
    public void countPostsByKeyword_shouldBeOK() throws Exception {
        this.client
                .get()
                .uri("/posts/count?q=0")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.count").isEqualTo(10);

        this.client
                .get()
                .uri("/posts/count?q=5")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.count").isEqualTo(19);

        this.client
                .get()
                .uri("/posts/count?q=post")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.count").isEqualTo(100);
    }


}
