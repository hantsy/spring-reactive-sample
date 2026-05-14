package com.example.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 *
 * @author hantsy
 */
@SpringJUnitConfig(classes = Application.class)
public class PostControllerTest {

    @Autowired
    PostController ctrl;

    WebTestClient client;

    @BeforeAll
    public void setup() {
        this.client = WebTestClient
            .bindToController(this.ctrl)
            .configureClient()
            .build();
    }

    @Test
    public void getAllPostsWillBeOk() throws Exception {
        this.client
            .get()
            .uri("/posts")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectBody()
            .jsonPath("$.length()")
            .isEqualTo(2);
    }

    @Test
    public void getPostById() throws Exception {
        this.client
            .get()
            .uri("/posts/1")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectBody()
            .jsonPath("$.title")
            .isEqualTo("post one");
        
        this.client
            .get()
            .uri("/posts/2")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectBody()
            .jsonPath("$.title")
            .isEqualTo("post two");
    }

}
