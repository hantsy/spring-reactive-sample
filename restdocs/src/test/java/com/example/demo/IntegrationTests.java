/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

/**
 * @author hantsy
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Application.class)
@Disabled
public class IntegrationTests {

    int port = 8080;

    WebTestClient rest;

    @BeforeAll
    public void setup() {
        this.rest = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofDays(1))
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @Test
    public void getAllPostsWillBeOk() throws Exception {
        this.rest
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus().isOk();
    }

}
