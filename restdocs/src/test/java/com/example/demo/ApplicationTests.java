/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 *
 * @author hantsy
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Application.class)
@ActiveProfiles("test")
public class ApplicationTests {

    @Autowired
    ApplicationContext context;

    WebTestClient rest;

    @BeforeAll
    public void setup() {
        this.rest = WebTestClient
            .bindToApplicationContext(this.context)
            .configureClient()
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
