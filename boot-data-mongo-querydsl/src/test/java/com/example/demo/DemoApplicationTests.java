package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.BDDMockito.given;

@WebFluxTest(controllers = PostController.class)
public class DemoApplicationTests {

    @Autowired
    WebTestClient client;

    @MockBean
    PostRepository posts;

    @Test
    public void getAllMessagesShouldBeOk() {
        Post post = new Post();
        post.setContent("content");
        given(posts.findAll()).willReturn(Flux.just(post));
        client.get().uri("/posts").exchange()
            .expectStatus().isOk();
    }

}
