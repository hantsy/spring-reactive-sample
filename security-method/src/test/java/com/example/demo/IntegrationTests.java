/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;
import java.util.function.Consumer;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

/**
 * @author hantsy
 */
@SpringJUnitConfig(classes = Application.class)
public class IntegrationTests {

    @Value("${server.port:8080}")
    int port;

    WebTestClient rest;

    @Autowired
    HttpServer httpServer;

    DisposableServer disposableServer;

    @BeforeEach
    public void setup() {
        this.disposableServer = this.httpServer.bindNow();
        this.rest = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofSeconds(10))
                .baseUrl("http://localhost:" + this.port)
                //.defaultHeaders(headers -> headers.setBasicAuth("user", "password"))
                .build();
    }

    @AfterEach
    public void teardown() {
        if (!this.disposableServer.isDisposed()) {
            this.disposableServer.disposeNow();
        }
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
                .headers(userCredentials())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    public void savingPostsWhenInvalidCredentialsThenUnauthorized() throws Exception {
        this.rest
                .post()
                .uri("/posts")
                .headers(invalidCredentials())
                .body(BodyInserters.fromValue(Post.builder().title("title test").content("content test").build()))
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
                .headers(userCredentials())
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    public void deletingPostsWhenUserCredentialsThenForbidden_mutateWith() throws Exception {
        this.rest
                .mutate().filter(basicAuthentication("user", "password")).build()
                .delete()
                .uri("/posts/1")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    private Consumer<HttpHeaders> userCredentials() {
        return httpHeaders -> httpHeaders.setBasicAuth("user", "password");
    }

    private Consumer<HttpHeaders> adminCredentials() {
        return httpHeaders -> httpHeaders.setBasicAuth("admin", "password");
    }

    private Consumer<HttpHeaders> invalidCredentials() {
        return httpHeaders -> httpHeaders.setBasicAuth("user", "INVALID");
    }
}
