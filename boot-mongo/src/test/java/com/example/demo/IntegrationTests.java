package com.example.demo;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {DemoApplication.class, ContainersConfig.class})
public class IntegrationTests {

  @Autowired
  WebTestClient client;

  @Test
  public void getAllMessagesShouldBeOk() {
    client.get().uri("/posts").exchange()
        .expectStatus().isOk();
  }

}
