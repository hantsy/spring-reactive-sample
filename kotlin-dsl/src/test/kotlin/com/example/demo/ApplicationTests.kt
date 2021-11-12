package com.example.demo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.kotlin.test.test

class ApplicationTests {

    lateinit var client: WebClient
    lateinit var app: Application

    @BeforeEach
    fun setup() {
        app = Application()
        app.start()
        Thread.sleep(5000)
        client = WebClient.create("http://localhost:8080")
    }

    @AfterEach
    fun tearDown() {
        app.stop()
    }

    @Test
    fun getPostsShouldBeOK() {
        client
            .get()
            .uri("/api/posts")
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
