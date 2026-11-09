package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringJUnitConfig(classes = {WebConfig.class, PostController.class, RestExceptionHandler.class})
public class PostControllerTest {

    @Autowired
    PostController postController;

    @Autowired
    RestExceptionHandler controllerAdvice;

    @MockitoBean
    PostRepository posts;

    WebTestClient client;

    @BeforeEach
    public void setup() {
        this.client = WebTestClient
                .bindToController(postController)
                .controllerAdvice(controllerAdvice)
                .configureClient()
                .build();
    }

    @Test
    public void testGetAllPosts() {
        given(this.posts.findAll())
                .willReturn(Flux.just(Post.of("test", "content")));

        this.client
                .get().uri("/posts").accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].title").isEqualTo("test");

        verify(this.posts, times(1)).findAll();
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void getPost_whenNotFound() {
        given(this.posts.findById(any(UUID.class)))
                .willReturn(Mono.empty());
        var postId = UUID.randomUUID();

        this.client
                .get().uri("/posts/{id}", postId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type").isEqualTo("https://example.com/errors/post-not-found")
                .jsonPath("$.id").isEqualTo(postId.toString())
                .jsonPath("$.entity").isEqualTo("POST");

        verify(this.posts, times(1)).findById(postId);
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void createPost_whenBodyInvalid() {
        this.client
                .post().uri("/posts")
                .contentType(APPLICATION_JSON)
                .bodyValue(Map.of("title", "test"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.errors[0].path").isEqualTo("content");

        verifyNoMoreInteractions(this.posts);
    }
}
