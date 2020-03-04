package com.example.demo;

import io.undertow.Undertow;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 *
 * @author hantsy
 */
public class IntegrationTests {

    static Undertow undertow = null;

    WebTestClient rest;

    @BeforeAll
    public static void beforeAll() throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(Application.class); 
        undertow = context.getBean(Undertow.class);
        undertow.start();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        if (undertow != null) {
            undertow.stop();
        }
    }

    @BeforeEach
    public void setup() {
        this.rest = WebTestClient
            .bindToServer()
            .responseTimeout(Duration.ofDays(1))
            .baseUrl("http://localhost:8080")
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
