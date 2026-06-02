package com.example.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {DemoApplication.class, TestcontainersConfiguration.class}
)
public class IntegrationTests {

    @LocalServerPort
    private int port;

    WebClient client;

    @BeforeEach
    public void setup() {
        this.client = WebClient.create("http://localhost:" + port);
    }

    @Test
    public void getAllMessagesShouldBeOk() {
        client.get().uri("/posts")
                .retrieve()
                .toEntityFlux(Post.class)
                .as(StepVerifier::create)
                .consumeNextWith(e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();
    }
}

