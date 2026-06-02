package com.example.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;


// mock web but real database.
@SpringBootTest(
        classes = {DemoApplication.class, TestcontainersConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
public class ApplicationTests {

    @Autowired
    PostController controller;

    WebTestClient client;

    @BeforeEach
    public void setup() {
//        this.client = WebTestClient.bindToApplicationContext(ctx)
//                .configureClient()
//                .build();

        this.client = WebTestClient.bindToController(controller)
                .configureClient()
                .build();
    }

    @Test
    public void getAllMessagesShouldBeOk() {
        client.get().uri("/posts").exchange()
                .expectStatus().isOk();
    }

}
