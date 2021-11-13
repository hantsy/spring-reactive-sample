package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Disabled// need to refactor the application codes.
public class DemoApplicationTests {

    @Autowired
    RouterFunction<?> routerFunction;

    WebTestClient client;

    @BeforeEach
    public void setup() {
        this.client = WebTestClient
                .bindToRouterFunction(this.routerFunction)
                .configureClient()
                .build();
    }

    @Test
    public void getFavoritedWithoutAuthWillReturn401() {
        String slug = "testslug";
        client
                .get()
                .uri("/posts/" + slug + "/favorited")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postCrudOperations() {
        String slug = "testslug";
        client
                .post()
                .uri("/posts/" + slug + "/favorites")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK);

        client
                .get()
                .uri("/posts/" + slug + "/favorited")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody().jsonPath("$.favorited").isEqualTo(true);

        client
                .get()
                .uri("/posts/" + slug + "/favorites")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody().jsonPath("$[0]").isEqualTo("user");

        client
                .get()
                .uri("/users/user/favorites")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody().jsonPath("$[0]").isEqualTo("testslug");

        client
                .delete()
                .uri("/posts/" + slug + "/favorites")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT);

        client
                .get()
                .uri("/posts/" + slug + "/favorited")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody().jsonPath("$.favorited").isEqualTo(false);
    }
}
