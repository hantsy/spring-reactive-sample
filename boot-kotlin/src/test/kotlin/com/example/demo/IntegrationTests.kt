package com.example.demo


import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

@SpringBootTest(classes = [DemoApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTests {

    private lateinit var client: WebClient

    @LocalServerPort
    private var port: Int = 8080

    @BeforeAll
    fun setup() {
        client = WebClient.create("http://localhost:$port")
    }

    @Test
    fun `get all posts`() {
        client.get()
            .uri("/posts")
            .accept(MediaType.APPLICATION_JSON)
            .exchangeToFlux {
                assertThat(it.statusCode()).isEqualTo(HttpStatus.OK)
                it.bodyToFlux(Post::class.java)
            }
            .`as` { StepVerifier.create(it) }
            .expectNextCount(2)
            .verifyComplete()
    }

}
