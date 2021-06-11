package com.example.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
public class ApplicationTests {

    @Autowired
    ApplicationContext ctx;

    @Autowired
    WebTestClient client;

    @Autowired
    PostController controller;

    @Test
    public void getAllMessagesShouldBeOk() {
        client.get().uri("/posts")
                .exchange()
                .expectStatus().isOk();
    }

}
