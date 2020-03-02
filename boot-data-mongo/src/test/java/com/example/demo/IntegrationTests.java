package com.example.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
//@AutoConfigureWebTestClient
public class IntegrationTests {

    @LocalServerPort
    int port;

     WebTestClient client;

//    @Autowired
//    WebTestClient client;

    @BeforeEach
    public void setup() {

        this.client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @Test
    public void getAllMessagesShouldBeOk() {
        client.get().uri("/posts").exchange()
            .expectStatus().isOk();
    }

}
