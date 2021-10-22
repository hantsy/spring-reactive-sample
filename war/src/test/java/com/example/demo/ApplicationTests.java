package com.example.demo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 *
 * @author hantsy
 */
@SpringJUnitConfig(classes = {AppConfig.class, WebConfig.class, SecurityConfig.class})
class ApplicationTests {

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
    void getAllPostsWillBeOk() {
        this.rest
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void deletingPostsWhenNoCredentialsThenUnauthorized() {
        this.rest
                .delete()
                .uri("/posts/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void deletingPostsWhenInvalidCredentialsThenUnauthorized() {
        this.rest
                .mutateWith(mockUser().password("WRONGPASSWORD"))
                .delete()
                .uri("/posts/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void deletingPostsWhenUserCredentialsThenForbidden() {
        this.rest
                .mutateWith(mockUser().password("password"))
                .delete()
                .uri("/posts/1")
                .exchange()
                .expectStatus().is4xxClientError();
    }

}
