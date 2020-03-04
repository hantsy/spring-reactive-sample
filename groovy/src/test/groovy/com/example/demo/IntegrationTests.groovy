package com.example.demo

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.netty.DisposableServer
import reactor.netty.http.server.HttpServer

import java.time.Duration

@SpringJUnitConfig(classes = [Application.class])
class IntegrationTests {

    @Value('${server.port:8080}')
    int port;

    WebTestClient rest

    @Autowired
    HttpServer httpServer

    private DisposableServer disposableServer

    @BeforeEach
    void setup() {
        this.disposableServer = this.httpServer.bindNow()
        this.rest = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofSeconds(10))
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @AfterEach
    void teardown() {
        this.disposableServer.dispose()
    }

    @Test
    void getAllPostsWillBeOk() throws Exception {
        this.rest
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus()
                .isOk();
    }

}
