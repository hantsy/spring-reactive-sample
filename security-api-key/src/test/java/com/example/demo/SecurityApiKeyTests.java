package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Application.class)
@ActiveProfiles("test")
class SecurityApiKeyTests {

    @Autowired
    ApplicationContext context;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        this.client = WebTestClient.bindToApplicationContext(this.context).build();
    }

    @Test
    void requestWithoutCredentialsIsUnauthorized() {
        this.client.get().uri("/user").exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void validApiKeyAuthenticates() {
        this.client.get().uri("/user").header("X-API-KEY", "user-api-key").exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo("user")
            .jsonPath("$.authenticated").isEqualTo(true);
    }

    @Test
    void invalidApiKeyIsUnauthorized() {
        this.client.get().uri("/user").header("X-API-KEY", "wrong-key").exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void basicCredentialsAuthenticate() {
        this.client.get().uri("/user").headers(h -> h.setBasicAuth("user", "password")).exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo("user")
            .jsonPath("$.authenticated").isEqualTo(true);
    }

    @Test
    void wrongBasicPasswordIsUnauthorized() {
        this.client.get().uri("/user").headers(h -> h.setBasicAuth("user", "wrong")).exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void basicReplacesApiKeyContextWhenBothHeadersPresent() {
        // Each mechanism runs in chain order; the later successful authentication
        // replaces the SecurityContext, so Basic (after the api-key filter) wins.
        this.client.get().uri("/user")
            .header("X-API-KEY", "admin-api-key")
            .headers(h -> h.setBasicAuth("user", "password"))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo("user");
    }

    @Test
    void apiKeyAuthenticationDoesNotCreateSession() {
        this.client.get().uri("/user").header("X-API-KEY", "user-api-key").exchange()
            .expectStatus().isOk()
            .expectHeader().doesNotExist("Set-Cookie");
    }

}