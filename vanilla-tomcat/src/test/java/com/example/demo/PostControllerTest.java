/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 *
 * @author hantsy
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@ActiveProfiles("test")
public class PostControllerTest {

    @Autowired
    PostController ctrl;

    WebTestClient rest;

    @Before
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
