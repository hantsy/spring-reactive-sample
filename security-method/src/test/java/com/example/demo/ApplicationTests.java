/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Map;
import java.util.function.Consumer;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.Credentials.basicAuthenticationCredentials;

/**
 * @author hantsy
 */
@SpringJUnitConfig(classes = Application.class)
@ActiveProfiles("test")
public class ApplicationTests {

    @Autowired
    ApplicationContext context;

    WebTestClient rest;

    @BeforeEach
    public void setup() {
        this.rest = WebTestClient
                .bindToApplicationContext(this.context)
                .apply(springSecurity())
                //.apply(csrf())
                .configureClient()
                //.defaultHeaders( headers ->headers.setBasicAuth("user", "password"))
                .build();
    }

    @Test
    public void getPostWhenNoCredentialsThenOK() throws Exception {
        this.rest
                .get()
                .uri("/posts/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.title").isEqualTo("post one");
    }

    @Test
    public void getAllPostsWhenNoCredentialsThenOk() throws Exception {
        this.rest
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    public void getAllPostsWhenValidCredentialsThenOk() throws Exception {
        this.rest
                .get()
                .uri("/posts")
                .attributes(userCredentials())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    public void savingPostsWhenInvalidCredentialsThenUnauthorized() throws Exception {
        this.rest
                .post()
                .uri("/posts")
                .body(BodyInserters.fromValue(Post.builder().title("title test").content("content test").build()))
                .attributes(invalidCredentials())
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    public void savingPostsWhenNoCredentialsThenUnauthorized() throws Exception {
        this.rest
                .post()
                .uri("/posts")
                .body(BodyInserters.fromValue(Post.builder().title("title test").content("content test").build()))
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    public void deletingPostsWhenUserCredentialsThenForbidden() throws Exception {
        this.rest
                .delete()
                .uri("/posts/1")
                .attributes(userCredentials())
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    public void deletingPostsWhenUserCredentialsThenForbidden_mutateWith() throws Exception {
        this.rest
                .mutateWith(mockUser().password("password"))
                .delete()
                .uri("/posts/1")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @WithMockUser()
    public void deletingPostsWhenUserCredentialsThenForbidden_withMockUserAnnotation() throws Exception {
        this.rest
                .delete()
                .uri("/posts/1")
                .exchange()
                .expectStatus().is4xxClientError();
    }

//    @Test
//    public void deletingPostsWhenAdminCredentialsThenOk() throws Exception {
//        this.rest
//            .delete()
//            .uri("/posts/1")
//            .attributes(adminCredentials())
//            .exchange()
//            .expectStatus().isOk()
//            .expectBody().isEmpty();
//    }

    private Consumer<Map<String, Object>> userCredentials() {
        return basicAuthenticationCredentials("user", "password");
    }

    private Consumer<Map<String, Object>> adminCredentials() {
        return basicAuthenticationCredentials("admin", "password");
    }

    private Consumer<Map<String, Object>> invalidCredentials() {
        return basicAuthenticationCredentials("user", "INVALID");
    }
}
