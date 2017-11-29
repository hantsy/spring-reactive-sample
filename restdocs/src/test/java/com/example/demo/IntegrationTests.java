/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

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
            .responseTimeout(Duration.ofDays(1))
            .baseUrl("http://localhost:" + this.port)
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
