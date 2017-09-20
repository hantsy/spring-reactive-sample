package com.example.demo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

/**
 *
 * @author hantsy
 */
public class IntegrationTests {

    WebTestClient rest;

    @Before
    public void setup() {
        this.rest = WebTestClient
            .bindToServer()
            .responseTimeout(Duration.ofDays(1))
            .baseUrl("http://localhost:8080")
            .filter(basicAuthentication())
            .build();
    }

    @Test
    public void getAllPostsWhenNoCredentialsThenOk() throws Exception {
        this.rest
            .get()
            .uri("/posts")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    public void deletingPostsWhenNoCredentialsThenUnauthorized() throws Exception {
        this.rest
            .delete()
            .uri("/posts/1")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    public void deletingPostsWhenInvalidCredentialsThenUnauthorized() throws Exception {
        this.rest
            .mutateWith(mockUser().password("WRONGPASSWORD"))
            .delete()
            .uri("/posts/1")
            .exchange()
            .expectStatus().isUnauthorized();
    }
    
     @Test
    public void deletingPostsWhenUserCredentialsThenForbidden() throws Exception {
        this.rest
            .mutateWith(mockUser().password("password"))
            .delete()
            .uri("/posts/1")
            .exchange()
            .expectStatus().is4xxClientError();
    }

}
