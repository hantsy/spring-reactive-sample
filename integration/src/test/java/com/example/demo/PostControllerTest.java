/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * @author hantsy
 */
@SpringJUnitConfig(classes = Application.class)
@ActiveProfiles("test")
public class PostControllerTest {

    @Autowired
    PostController ctrl;

    WebTestClient rest;

    @BeforeEach
    public void setup() {
        this.rest = WebTestClient
                .bindToController(this.ctrl)
                .configureClient()
                .build();
    }

    @Test
    public void getAllPostsWillBeOk() throws Exception {
        this.rest
                .get()
                .uri("/posts")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectBody()
                .jsonPath("$.length()")
                .isEqualTo(2);
    }

    @Test
    public void getPostById() throws Exception {
        this.rest
                .get()
                .uri("/posts/1")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectBody()
                .jsonPath("$.title")
                .isEqualTo("post one");

        this.rest
                .get()
                .uri("/posts/2")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectBody()
                .jsonPath("$.title")
                .isEqualTo("post two");
    }

}
