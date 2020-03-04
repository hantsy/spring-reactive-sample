/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author hantsy
 */
@SpringJUnitConfig(classes = Application.class)
@ActiveProfiles("test")
public class ApplicationTests {

    @Autowired
    ApplicationContext context;

    WebTestClient client;

    @BeforeAll
    public void setup() {
        this.client = WebTestClient
                .bindToApplicationContext(this.context)
                .configureClient()
                .build();
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
