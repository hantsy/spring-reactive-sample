package com.example.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class IntegrationTests {

    @Autowired
    WebTestClient webClient;

    @Test
    public void willLoadPosts() {
    	this.webClient.get().uri("/posts")
				.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBodyList(Post.class).hasSize(3);
    }

}

