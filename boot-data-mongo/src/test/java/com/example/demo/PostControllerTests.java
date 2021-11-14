package com.example.demo;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = PostController.class)
public class PostControllerTests {

    @Autowired
    WebTestClient client;

    @MockBean
    PostRepository posts;

    @Test
    public void getAllMessagesShouldBeOk() {
        Post post1 = Post.builder().content("my test content").title("my test title").build();
        Post post2 = Post.builder().content("content of another post").title("another post title").build();
        given(this.posts.findAll()).willReturn(Flux.just(post1, post2));

        client.get().uri("/posts").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.[0].title").isEqualTo("my test title")
                .jsonPath("$.[1].title").isEqualTo("another post title");

        verify(this.posts, times(1)).findAll();
        verifyNoMoreInteractions(this.posts);
    }

}
