package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(controllers = PostController.class)
class PostControllerTests {

    @Autowired
    WebTestClient client;

    @MockBean
    PostRepository postRepository;

    @Test
    void getAllMessagesShouldBeOk() {
        client.get().uri("/posts").exchange()
            .expectStatus().isOk();
    }

}