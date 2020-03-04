package com.example.demo;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

@SpringJUnitConfig(classes = Application.class)
public class IntegrationTests {

    @Autowired
    Tomcat tomcat;

    WebTestClient client;

    @BeforeAll
    public void beforeAll() throws LifecycleException {
        tomcat.start();
        System.out.println("Tomcat server is running at port:" + tomcat.getConnector().getLocalPort());
    }

    @AfterAll
    public void afterAll() throws LifecycleException {
        tomcat.stop();
        tomcat.destroy();
    }

    @BeforeEach
    public void setup() {
        this.client = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofSeconds(300))
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Test
    public void getAllPostsWillBeOk() throws Exception {
        this.client
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus().isOk();
    }

}
