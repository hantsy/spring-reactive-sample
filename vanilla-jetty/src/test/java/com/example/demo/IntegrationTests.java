package com.example.demo;

import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

/**
 *
 * @author hantsy
 */
public class IntegrationTests {

    static Server jetty = null;

    WebTestClient client;

    @BeforeAll
    public static void beforeAll() throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(Application.class); 
        jetty = context.getBean(Server.class);
        jetty.start();
        //jetty.join();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        if (jetty != null) {
            jetty.stop();
        }
    }

    @BeforeEach
    public void setup() {
        this.client = WebTestClient
            .bindToServer()
            .responseTimeout(Duration.ofDays(1))
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
