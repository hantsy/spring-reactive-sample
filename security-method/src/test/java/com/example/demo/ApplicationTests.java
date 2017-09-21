/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.Map;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
public class ApplicationTests {

    @Autowired
    ApplicationContext context;

    WebTestClient rest;

    @Before
    public void setup() {
        this.rest = WebTestClient
            .bindToApplicationContext(this.context)
            .apply(springSecurity())
            .configureClient()
            .filter(basicAuthentication())
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
            .body(BodyInserters.fromObject(Post.builder().title("title test").content("content test").build()))
            .attributes(invalidCredentials())
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
            .mutateWith(mockUser().password("password"))
            .delete()
            .uri("/posts/1")
            .exchange()
            .expectStatus().is4xxClientError()
            .expectBody().isEmpty();
    }

    @Test
    @WithMockUser()
    public void deletingPostsWhenUserCredentialsThenForbidden_withMockUserAnnotation() throws Exception {
        this.rest
            .delete()
            .uri("/posts/1")
            .exchange()
            .expectStatus().is4xxClientError()
            .expectBody().isEmpty();
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
