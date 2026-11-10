package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = Application.class)
@ActiveProfiles("test")
public class ApplicationTests {

    private static final String ALLOWED_ORIGIN = "https://allowed-origin.com";

    @Autowired
    ApplicationContext context;

    WebTestClient rest;

    @BeforeEach
    public void setup() {
        this.rest = WebTestClient
                .bindToApplicationContext(this.context)
                .configureClient()
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Test
    public void preflightRequest_shouldReturnCorsHeaders() {
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
    public void getAllPostsWithAllowedOrigin_shouldReturnAllowOriginHeader() {
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
    public void createPostWithAllowedOrigin_shouldReturnAllowOriginHeader() {
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
    public void getAllPostsWithDisallowedOrigin_shouldReturn403() {
        this.rest
                .get().uri("/posts")
                .header(HttpHeaders.ORIGIN, "https://disallowed-origin.com")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void getAllPostsWithoutOrigin_shouldNotReturnCorsHeaders() {
        this.rest
                .get().uri("/posts")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    }

}