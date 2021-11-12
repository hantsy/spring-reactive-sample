package com.example.demo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.web.reactive.function.client.WebClient
import reactor.kotlin.test.test

@SpringJUnitConfig(classes = [Application::class])
class ApplicationTests {

    @Value("\${server.port:8080}")
    var port = 8080

    lateinit var client: WebClient

    @BeforeEach
    fun setup() {
        client = WebClient.create("http://localhost:${this.port}")
    }

    @Test
    fun getPostsShouldBeOK() {
        client
            .get()
            .uri("/posts")
            .accept(MediaType.APPLICATION_JSON)
            .exchangeToFlux {
                assertThat(it.statusCode()).isEqualTo(HttpStatus.OK)
                it.bodyToFlux(Post::class.java)
            }
            .test()
            .expectNextCount(2)
            .verifyComplete()
    }

}
