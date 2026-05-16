package com.example.demo


import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient

import java.time.Duration

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [ContainersConfig.class])
class IntegrationTests {

    @Autowired
    WebTestClient client

    @Test
    void getAllPostsWillBeOk() throws Exception {
        this.client
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus()
                .isOk();
    }

}
