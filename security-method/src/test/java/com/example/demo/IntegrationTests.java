/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.Credentials.basicAuthenticationCredentials;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

/**
 *
 * @author hantsy
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class IntegrationTests {

    @Value("#{@nettyContext.address().getPort()}")
    int port;

    WebTestClient rest;

    @Before
    public void setup() {
        this.rest = WebTestClient
            .bindToServer()
            .responseTimeout(Duration.ofSeconds(10))
            .baseUrl("http://localhost:" + this.port)
            .filter(basicAuthentication())
            .build();
    }

    @After
    public void teardown() {

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
            .attributes(invalidCredentials())
            .body(BodyInserters.fromObject(Post.builder().title("title test").content("content test").build()))
            .exchange()
            .expectStatus().isUnauthorized()
            .expectBody().isEmpty();
    }

    @Test
    public void savingPostsWhenNoCredentialsThenUnauthorized() throws Exception {
        this.rest
            .post()
            .uri("/posts")
            .body(BodyInserters.fromObject(Post.builder().title("title test").content("content test").build()))
            .exchange()
            .expectStatus().isUnauthorized()
            .expectBody().isEmpty();
    }

    @Test
    public void deletingPostsWhenUserCredentialsThenForbidden() throws Exception {
        this.rest
            .delete()
            .uri("/posts/1")
            .attributes(userCredentials())
            .exchange()
            .expectStatus().is4xxClientError()
            .expectBody().isEmpty();
    }

    @Test
    public void deletingPostsWhenUserCredentialsThenForbidden_mutateWith() throws Exception {
        this.rest
            .mutate().filter(basicAuthentication("user", "password")).build()
            .delete()
            .uri("/posts/1")
            .exchange()
            .expectStatus().is4xxClientError()
            .expectBody().isEmpty();
    }

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
