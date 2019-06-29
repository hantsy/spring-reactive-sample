/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.time.Duration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/**
 * @author hantsy
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class IntegrationTests {

    @Value("${server.port:8080}")
    int port;

    WebTestClient rest;

    @Autowired
    HttpServer httpServer;

    private DisposableServer disposableServer;

    @Before
    public void setup() {
        this.disposableServer = this.httpServer.bindNow();
        this.rest = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofDays(1))
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @After
    public void teardown() {
        this.disposableServer.dispose();
    }

    @Test
    public void getAllPostsWillBeOk() throws Exception {
        this.rest
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus()
                .isOk();
    }

}
