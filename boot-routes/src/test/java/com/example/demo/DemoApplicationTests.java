package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.net.URI;
import java.util.Objects;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

    @Autowired
    WebTestClient client;

    @Test
    void getAllPostsShouldBeOkWithAuthentication() {
        client
            .get()
            .uri("/posts/")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void getANoneExistedPostShouldReturn404() {
        client
            .get()
            .uri("/posts/ABC")
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void createAPostNotAllowedWhenIsNotAuthorized() {
        client.mutateWith(csrf())
            .post()
            .uri("/posts")
            .body(BodyInserters.fromValue(Post.builder().title("Post test").content("content of post test").build()))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deletePostsNotAllowedWhenIsNotAdmin() {
        client
            .mutate().filter(basicAuthentication("user", "password")).build()
            .delete()
            .uri("/posts/1")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void postCrudOperations() {
        int randomInt = new Random().nextInt();
        String title = "Post test " + randomInt;
        FluxExchangeResult<Void> postResult = client
                .mutateWith(mockUser().roles("USER"))
                .mutateWith(csrf())
            .post()
            .uri("/posts")
            .body(BodyInserters.fromValue(Post.builder().title(title).content("content of " + title).build()))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CREATED)
            .returnResult(Void.class);

        URI location = postResult.getResponseHeaders().getLocation();
        assertThat(location).isNotNull();

        EntityExchangeResult<byte[]> getResult = client
            .get()
            .uri(location)
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$.title").isEqualTo(title)
            .returnResult();

        String getPost = new String(Objects.requireNonNull(getResult.getResponseBody()));
        assertThat(getPost.contains(title)).isTrue();
    }

}
