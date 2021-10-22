package com.example.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
@ContextConfiguration(initializers = {MongodbContainerInitializer.class})
@AutoConfigureWebTestClient
public class IntegrationTests {

    @Autowired
    WebTestClient client;

    @Test
    public void getAllMessagesShouldBeOk() {
        client.get().uri("/posts").exchange()
            .expectStatus().isOk();
    }

}
