package com.example.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {DemoApplication.class, ContainersConfig.class})
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
