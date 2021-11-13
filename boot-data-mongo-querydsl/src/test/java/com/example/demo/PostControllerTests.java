package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.*;

@WebFluxTest(controllers = PostController.class)
public class PostControllerTests {

    @Autowired
    WebTestClient client;

    @MockBean
    PostRepository posts;

    @Test
    public void getAllPosts() {
        when(posts.findAll()).thenReturn(
                Flux.just(
                        Post.builder().title("post one").content("The content of post one").build()
                )
        );
        client.get().uri("/posts")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.size()").isEqualTo(1);

        verify(this.posts, times(1)).findAll();
        verifyNoMoreInteractions(this.posts);
    }

}
