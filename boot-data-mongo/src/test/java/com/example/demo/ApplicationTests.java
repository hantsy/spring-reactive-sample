package com.example.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest()
public class ApplicationTests {

    @Autowired
    ApplicationContext ctx;

    WebTestClient client;

    @Autowired
    PostController controller;

    @BeforeEach
    public void setup() {
        /**
         this.client= WebTestClient.bindToApplicationContext(ctx)
         .configureClient()
         .build();
         **/

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
