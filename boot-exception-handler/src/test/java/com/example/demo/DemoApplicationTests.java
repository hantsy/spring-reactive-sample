package com.example.demo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @Autowired
    ApplicationContext applicationContext;

    WebTestClient client;

    @Before
    public void setup(){
        client = WebTestClient.bindToApplicationContext(applicationContext)
            .configureClient()
            .build();
    }

    @Test
    public void getNoneExistedPost_shouldReturn404() {
        client.get().uri("/posts/xxxx").exchange()
            .expectStatus().isNotFound();
    }

}
