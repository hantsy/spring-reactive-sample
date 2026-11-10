package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class DemoApplicationTests {

    private static final String ALLOWED_ORIGIN = "https://allowed-origin.com";
    private static final String HELLO_ORIGIN = "https://hello-origin.com";

    @Autowired
    WebTestClient rest;

    @Test
    void preflightRequest_shouldReturnCorsHeaders() {
        this.rest
                .options().uri("/posts")
                .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN)
                .expectHeader().value(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        methods -> assertTrue(methods.contains("POST")))
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
    }

    @Test
    void getAllPostsWithAllowedOrigin_shouldReturnAllowOriginHeader() {
        this.rest
                .get().uri("/posts")
                .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN)
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("post one");
    }

    @Test
    void createPostWithAllowedOrigin_shouldReturnAllowOriginHeader() {
        this.rest
                .post().uri("/posts")
                .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"new post\",\"content\":\"content of new post\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN)
                .expectBody()
                .jsonPath("$.title").isEqualTo("new post");
    }

    @Test
    void getAllPostsWithDisallowedOrigin_shouldReturn403() {
        this.rest
                .get().uri("/posts")
                .header(HttpHeaders.ORIGIN, "https://disallowed-origin.com")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getAllPostsWithoutOrigin_shouldNotReturnCorsHeaders() {
        this.rest
                .get().uri("/posts")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    }

    @Test
    void helloWithAllowedOrigin_shouldReturnAllowOriginHeader() {
        this.rest
                .get().uri("/hello")
                .header(HttpHeaders.ORIGIN, HELLO_ORIGIN)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, HELLO_ORIGIN)
                .expectBody(String.class).isEqualTo("Hello CORS");
    }

    @Test
    void helloWithDisallowedOrigin_shouldReturn403() {
        this.rest
                .get().uri("/hello")
                .header(HttpHeaders.ORIGIN, "https://other-origin.com")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void helloWithoutOrigin_shouldNotReturnCorsHeaders() {
        this.rest
                .get().uri("/hello")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
                .expectBody(String.class).isEqualTo("Hello CORS");
    }

}