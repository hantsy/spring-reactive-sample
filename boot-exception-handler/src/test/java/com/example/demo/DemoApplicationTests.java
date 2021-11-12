package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
public class DemoApplicationTests {

    @Autowired
    ApplicationContext applicationContext;

    WebTestClient client;

    @BeforeEach
    public void setup() {
        client = WebTestClient.bindToApplicationContext(applicationContext)
                .configureClient()
                .build();
    }

    @Test
    public void getNoneExistedPost_shouldReturn404() {
        client.get().uri("/posts/xxxx").exchange()
                .expectStatus().isNotFound();
    }

}
