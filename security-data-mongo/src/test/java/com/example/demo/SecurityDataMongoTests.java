package com.example.demo;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.mongodb.MongoDBContainer;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Application.class, initializers = SecurityDataMongoTests.MongoInitializer.class)
@ActiveProfiles("test")
class SecurityDataMongoTests {

    static class MongoInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            MongoDBContainer container = new MongoDBContainer("mongo:8");
            container.start();
            context.addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> container.stop());
            context.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("testproperties",
                            Map.of("mongo.uri", "mongodb://localhost:" + container.getFirstMappedPort())));
        }
    }

    @Autowired
    ApplicationContext context;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        this.client = WebTestClient.bindToApplicationContext(this.context).build();
    }

    @Test
    void currentUserRequiresAuthentication() {
        this.client.get().uri("/user")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void currentUserReturnsThePrincipal() {
        this.client.get().uri("/user")
                .headers(h -> h.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.username").isEqualTo("user");
    }

    @Test
    void currentUserDoesNotExposeThePassword() {
        this.client.get().uri("/user")
                .headers(h -> h.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.password").doesNotExist();
    }

    @Test
    void userPathRequiresAuthentication() {
        this.client.get().uri("/users/user")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void ownUserPathIsGranted() {
        this.client.get().uri("/users/user")
                .headers(h -> h.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.username").isEqualTo("user");
    }

    @Test
    void otherUserPathIsDenied() {
        this.client.get().uri("/users/admin")
                .headers(h -> h.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void adminCannotImpersonateAnotherUserPath() {
        this.client.get().uri("/users/user")
                .headers(h -> h.setBasicAuth("admin", "password"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void adminOwnPathIsGranted() {
        this.client.get().uri("/users/admin")
                .headers(h -> h.setBasicAuth("admin", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.username").isEqualTo("admin");
    }
}