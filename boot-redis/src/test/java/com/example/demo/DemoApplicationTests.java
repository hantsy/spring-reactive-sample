package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@SpringBootTest()
public class DemoApplicationTests {

    @Autowired
    // RouterFunction<ServerResponse> routerFunction;
    ApplicationContext applicationContext;

    WebTestClient client;

    @BeforeEach
    public void setup() {
        this.client = WebTestClient
                //.bindToRouterFunction(this.routerFunction)
                .bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @Test
    public void getAllPosts() {
        client
                .get()
                .uri("/posts/")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void getANoneExistedPostShouldReturn404() {
        client
                .get()
                .uri("/posts/"+UUID.randomUUID().toString())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void getFavoritedWithoutAuthWillReturn401() {
        String slug = UUID.randomUUID().toString();
        client
                .get()
                .uri("/posts/" + slug + "/favorited")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void getFavoritedWithAuthWillReturnOK() {
        String slug = UUID.randomUUID().toString();
        client.get()
                .uri("/posts/" + slug + "/favorited").accept(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody().json("{\"favorited\":false}");
    }

    @Test
    public void postCrudOperations() {
        String slug = UUID.randomUUID().toString();
        client.mutateWith(mockUser())
                .post()
                .uri("/posts/" + slug + "/favorites")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK);

        client.mutateWith(mockUser())
                .get()
                .uri("/posts/" + slug + "/favorited")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody().jsonPath("$.favorited").isEqualTo(true);

        client.mutateWith(mockUser())
                .get()
                .uri("/posts/" + slug + "/favorites")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody().jsonPath("$[0]").isEqualTo("user");

        client.mutateWith(mockUser())
                .get()
                .uri("/users/user/favorites")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody().jsonPath("$[0]").isEqualTo(slug);

        client.mutateWith(mockUser())
                .delete()
                .uri("/posts/" + slug + "/favorites")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT);

        client.mutateWith(mockUser())
                .get()
                .uri("/posts/" + slug + "/favorited")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody().jsonPath("$.favorited").isEqualTo(false);
    }
}
