package com.example.demo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import io.undertow.Undertow;
import java.time.Duration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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

    @BeforeClass
    public static void beforeAll() throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(Application.class); 
        undertow = context.getBean(Undertow.class);
        undertow.start();
    }

    @AfterClass
    public static void afterAll() throws Exception {
        if (undertow != null) {
            undertow.stop();
        }
    }

    @Before
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
