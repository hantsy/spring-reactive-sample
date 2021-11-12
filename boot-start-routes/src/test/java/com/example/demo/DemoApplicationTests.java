package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
public class DemoApplicationTests {

    @Autowired
    WebTestClient client;

    @Test
    public void getAllPosts() {
        client.get().uri("/posts").exchange().expectStatus().isOk();
    }

}
