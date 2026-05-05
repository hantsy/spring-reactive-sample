package com.example.demo;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@WebFluxTest(controllers = PostController.class)
public class PostControllerTest {

    @Autowired
    WebTestClient client;

    @MockitoBean
    PostRepository posts;

    @Test
    public void getAllPosts() {
        when(this.posts.findAll()).thenReturn(Flux.just(
                Post.builder().id(UUID.randomUUID()).title("post one").content("content of post one").build(),
                Post.builder().id(UUID.randomUUID()).title("post two").content("content of post two").build()
        ));
        client.get().uri("/posts")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.size()").isEqualTo(2);

        verify(this.posts, times(1)).findAll();
        verifyNoMoreInteractions(this.posts);
    }

}
