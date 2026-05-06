package com.example.demo;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@AutoConfigureWebTestClient
@SpringBootTest
public class ApplicationTest {

    @Autowired
    PostController ctl;

    @MockitoBean
    PostRepository posts;

    WebTestClient client;

    @BeforeEach
    void setUp() {
//        this.client = WebTestClient.bindToApplicationContext(context)
//            .configureClient()
//            .build();
        this.client = WebTestClient.bindToController(this.ctl)
            .configureClient()
            .build();
    }

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
