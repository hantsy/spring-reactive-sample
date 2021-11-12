package com.example.demo

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient

@SpringJUnitConfig(classes = [Application.class])
@ActiveProfiles("test")
class ApplicationTests {

    @Autowired
    ApplicationContext context

    WebTestClient client

    @BeforeEach
    void setup() {
        client = WebTestClient.bindToApplicationContext(context)
                .configureClient()
                .build();
    }

    @Test
    void contextLoads() {
        client.get()
                .uri("/posts")
                .exchange()
                .expectStatus().isOk()
    }

}
