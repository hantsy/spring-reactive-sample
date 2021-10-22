package com.example.demo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.time.Duration;


import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

/**
 * @author hantsy
 */
class IntegrationTests {

    WebTestClient rest;

    @BeforeAll
    public void setup() {
        this.rest = WebTestClient
                .bindToServer()
                //.defaultHeaders(headers -> headers.setBasicAuth("user", "password"))
                .responseTimeout(Duration.ofDays(1))
                .baseUrl("http://localhost:9000")
                .build();
    }

    @Test
    void getAllPostsWhenNoCredentialsThenOk() {
        this.rest
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void deletingPostsWhenNoCredentialsThenUnauthorized() {
        this.rest
                .delete()
                .uri("/posts/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    //replace mutateWith(mockUser()) with mutate().mutate().filter(basicAuthentication("user", "WRONGPASSWORD")).build()
    //in e2e tests.
    @Test
    void deletingPostsWhenInvalidCredentialsThenUnauthorized() {
        this.rest
                .mutate().filter(basicAuthentication("user", "WRONGPASSWORD")).build()
                .delete()
                .uri("/posts/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void deletingPostsWhenUserCredentialsThenForbidden() {
        this.rest
                .mutate().filter(basicAuthentication("user", "password")).build()
                .delete()
                .uri("/posts/1")
                .exchange()
                .expectStatus().is4xxClientError();
    }
}
