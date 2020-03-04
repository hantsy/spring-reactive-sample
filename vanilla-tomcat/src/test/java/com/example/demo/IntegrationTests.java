package com.example.demo;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

public class IntegrationTests {

    Tomcat tomcat = null;

    WebTestClient rest;

    @BeforeAll
    public void beforeAll() throws LifecycleException {
        ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
        tomcat = context.getBean(Tomcat.class);
        tomcat.start();
    }

    @AfterAll
    public void afterAll() throws LifecycleException {
        if (tomcat != null) {
            tomcat.stop();
        }
    }

    @BeforeEach
    public void setup() {
        this.rest = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofSeconds(10))
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
