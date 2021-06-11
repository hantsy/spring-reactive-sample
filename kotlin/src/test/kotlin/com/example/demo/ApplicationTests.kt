package com.example.demo

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.test

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [Application::class])
class ApplicationTests {

    @Value("#{@nettyHttpServer.port()}")
    var port = 8080

    lateinit var client: WebClient

    @BeforeAll
    fun setup() {
        client = WebClient.create("http://localhost:" + this.port)
    }

    @Test
    fun getPostsShouldBeOK() {
        client
                .get()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .test()
                .expectNextMatches { it.statusCode() == HttpStatus.OK }
                .verifyComplete()
    }

}
