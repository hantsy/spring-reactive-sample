package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.mockito.Mockito.*;

@SpringBootTest
// create a `WebTestClient` with essential options from application context automaticially, it works on both mock env or a real server.
// @AutoConfigureWebTestClient
public class ApplicationTest {

    @Autowired
    RouterFunction<ServerResponse> routerFunction;

    @MockBean
    PostRepository posts;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        // build from Spring `ApplicationContext`        
//        this.client = WebTestClient.bindToApplicationContext(context)
//            .configureClient()
//            .build();
//

        // build from a `WebHandler` bean
//        this.client = WebTestClient.bindToWebHandler(RouterFunctions.toWebHandler(this.routerFunction))
//                .configureClient()
//                .build();

        this.client = WebTestClient.bindToRouterFunction(this.routerFunction)
                .configureClient()
                .build();
    }

    @Test
    public void getAllPosts() {
        when(this.posts.findAll()).thenReturn(Flux.just(
                Post.builder().id(UUID.randomUUID()).title("post one").content("content of post one")
                        .build(),
                Post.builder().id(UUID.randomUUID()).title("post two").content("content of post two")
                        .build()
        ));
        client.get().uri("/posts")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.size()").isEqualTo(2);

        verify(this.posts, times(1)).findAll();
        verifyNoMoreInteractions(this.posts);
    }

}
