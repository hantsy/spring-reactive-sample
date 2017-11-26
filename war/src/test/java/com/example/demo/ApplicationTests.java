package com.example.demo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 *
 * @author hantsy
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {WebConfig.class, SecurityConfig.class})
public class ApplicationTests {

    @Autowired
    ApplicationContext context;

    WebTestClient rest;

    @Before
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
