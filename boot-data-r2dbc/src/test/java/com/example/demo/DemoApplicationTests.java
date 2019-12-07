package com.example.demo;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemoApplicationTests {

    @LocalServerPort
    int port;
    WebTestClient webClient;

    @Before
    public void setup() {
        this.webClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @Test
    public void willLoadPosts() {
    	this.webClient.get().uri("/posts")
				.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBodyList(Post.class).hasSize(1);
    }

}

